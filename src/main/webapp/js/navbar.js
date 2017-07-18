/**
 * Created by THU73 on 17/7/14.
 */
visit = function (target) {
    var form = document.createElement('form');
    form.action = '/' + target;

    form.target = '_self';
    form.method = 'post';

    var opt = document.createElement('input');
    opt.type = 'hidden';
    opt.name = 'username';
    opt.value = $.cookie('username');
    form.appendChild(opt);

    document.body.appendChild(form);
    form.submit();
}

