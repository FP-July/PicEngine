package raytracing.photon;

import raytracing.Vec3d;
import raytracing.kdtree.KDFactory;
import raytracing.kdtree.KDTree;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by THU73 on 17/7/26.
 */
public class PhotonLoader {
    KDTree tree;
    ArrayList<Photon> list;

    PhotonLoader(String filePath) {
        list = new ArrayList<>();
        try {
            File file = new File(filePath);
            if (file.isFile() && file.exists()) { // 判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file));// 考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;

                while ((lineTxt = bufferedReader.readLine()) != null) {
                    String[] info = lineTxt.split("\t");
                    assert (info.length == 4);
                    double x = Double.parseDouble(info[0]);
                    double y = Double.parseDouble(info[1]);
                    double z = Double.parseDouble(info[2]);
                    double intensity = Double.parseDouble(info[3]);

                    Vec3d position = new Vec3d(x, y, z);
                    Photon photon = new Photon();
                    photon.setPosition(position);
                    photon.setStrength(intensity);
                    list.add(photon);
                }
                bufferedReader.close();
                read.close();
            } else {
                System.out.println("找不到指定的文件");
            }
        } catch (Exception e) {
            System.out.println("读取文件内容出错");
            e.printStackTrace();
        }
        tree = KDFactory.generate((Photon[]) list.toArray());
    }

    public Photon getNearest(Vec3d position) {
        Photon p = new Photon();
        p.setPosition(position);
        return (Photon) tree.nearest(p);
    }
}
