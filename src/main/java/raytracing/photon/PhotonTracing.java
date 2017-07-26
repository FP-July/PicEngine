package raytracing.photon;

import raytracing.Ray;
import raytracing.Vec3d;
import raytracing.model.Primitive;
import raytracing.model.Sphere;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by THU73 on 17/7/26.
 */
public class PhotonTracing {

    private List<Primitive> scene = new ArrayList<Primitive>();
    private int MAX_RAY_DEPTH = 5;
    private ArrayList<Photon> photonList = new ArrayList<>();

    public List<Primitive> getScene() {
        return scene;
    }

    public void setMaxRayDepth(String depth) {
        MAX_RAY_DEPTH = Integer.parseInt(depth);
    }

    public void emitPhoton(int count) {

        Primitive lightSource = null;
        for (Primitive item : scene) {
            if (item.isLight() && item instanceof Sphere) {
                lightSource = item;

                Sphere light = (Sphere) lightSource;

                for (int i = 0; i < count; ++i) {
                    double horizontalAngle = Math.random() * 2 * Math.PI;
                    double verticalAngle = Math.random() * Math.PI;

                    double x = Math.sin(verticalAngle) * Math.cos(horizontalAngle);
                    double y = Math.sin(verticalAngle) * Math.sin(horizontalAngle);
                    double z = Math.cos(verticalAngle);

                    Vec3d photonDirection = new Vec3d(x, y, z);
                    Vec3d startPoint = light.center.add(photonDirection.mul(light.radius));

                    Ray ray = new Ray(startPoint, photonDirection);

                    double intensity = (light.emissionColor.x + light.emissionColor.y + light.emissionColor.z) / 3;

                    tracePhoton(ray, 0, intensity);
                }

            }
        }
        if (lightSource == null) {
            System.out.println("No lightSource!");
            return;
        }

    }

    public void tracePhoton(Ray ray, int depth, double intensity) {
        if (depth > MAX_RAY_DEPTH) {
            return;
        }

        double tnear = Double.MAX_VALUE;
        Primitive obj = null;
        // find intersection of this ray with the sphere in the scene
        for (Primitive sc : scene) {
            List<Double> pHits = new ArrayList<Double>();
            if (sc.intersect(ray, pHits)) {
                for (Double phit : pHits) {
                    if (phit < 0) continue;
                    if (phit < tnear) {
                        tnear = phit;
                        obj = sc;
                        break;
                    }
                }
            }
        }

        // if there's no intersection return black or background color
        if (obj == null) return;

        Vec3d phit = ray.cross(tnear); // point of intersection
        Vec3d nhit = obj.getNormal(phit); // normal at the intersection point
        nhit.normalize(); // normalize normal direction


        double bias = 1e-4; // add some bias to the point from which we will be tracing
        boolean inside = false;
        if (ray.raydir.dot(nhit) > 0) {
            nhit = nhit.inv();
            inside = true;
        }
        if ((obj.getTransparency(phit) > 0 || obj.getReflection(phit) > 0) && depth < MAX_RAY_DEPTH) {
            double facingratio = -ray.raydir.dot(nhit);
            // change the mix value to tweak the effect
            double fresneleffect = mix(Math.pow(1 - facingratio, 3), 1, 0.1);
            // compute reflection direction (not need to normalize because all vectors
            // are already normalized)
//            double rand = Math.random();
//            if(rand < fresneleffect) {
                Vec3d refldir = ray.raydir.sub(nhit.mul(2.0).mul(ray.raydir.dot(nhit)));
                refldir.normalize();
                tracePhoton(new Ray(phit.add(nhit.mul(bias)), refldir), depth + 1, intensity * fresneleffect);
                // little change in the final effect
                Photon photon = new Photon();
                photon.setPosition(phit);
                photon.setStrength(intensity * fresneleffect);
                photonList.add(photon);
//            }

            // if the sphere is also transparent compute refraction ray (transmission)
            if (obj.getTransparency(phit) != 0) {
//                rand = Math.random();
//                if(rand < (1 - fresneleffect)) {
                    double ior = 1.1, eta = (inside) ? ior : 1 / ior; // are we inside or outside the surface?
                    double cosi = -nhit.dot(ray.raydir);
                    double k = 1 - eta * eta * (1 - cosi * cosi);
                    Vec3d refrdir = ray.raydir.mul(eta).add(nhit.mul((eta * cosi - Math.sqrt(k))));
                    refrdir.normalize();
                    tracePhoton(new Ray(phit.sub(nhit.mul(bias)), refrdir), depth + 1, intensity * (1 - fresneleffect));

//                    Photon photon = new Photon();
//                    photon.setPosition(phit);
//                    photon.setStrength(intensity * (1 - fresneleffect));
//                    photonList.add(photon);
//                }
            }
            // the result is a mix of reflection and refraction (if the sphere is transparent)
        } else {

            // it's a diffuse object, no need to raytrace any further
            Photon photon = new Photon();
            photon.setPosition(phit);
            photon.setStrength(intensity);
            count ++;
            photonList.add(photon);
        }
    }
    private static int count = 0;

    private double mix(double a, double b, double mix) {
        return b * mix + a * (1 - mix);
    }

    public void printFile(String path) throws IOException {
    	System.out.println(count + ", " + photonList.size());
        File file = new File(path);
        if(!file.exists()) {
            file.createNewFile(); // 创建新文件
        }
        
        BufferedWriter out = new BufferedWriter(new FileWriter(file, false));
        for(int i = 0; i < photonList.size(); ++i) {
            Photon p = photonList.get(i);
            double x = p.getPosition().x;
            double y = p.getPosition().y;
            double z = p.getPosition().z;
            double intensity = p.getStrength();
            String info = x + "\t" + y + "\t" + z + "\t" + intensity + "\n";
            out.write(info); // \r\n即为换行
        }
        out.close(); // 最后记得关闭文件
    }
}
