package com.yy.lqw.app.svga;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private SimpleDraweeView mShowImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mShowImage = (SimpleDraweeView) findViewById(R.id.iv_showImage);
        findViewById(R.id.btn_porsche).setOnClickListener(this);
        findViewById(R.id.btn_rose).setOnClickListener(this);
        findViewById(R.id.btn_angel).setOnClickListener(this);
        findViewById(R.id.btn_cupid1).setOnClickListener(this);
        findViewById(R.id.btn_cupid2).setOnClickListener(this);
        findViewById(R.id.btn_stop).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_porsche:
//                showImage("http://ourtimespicture.bs2dl.yy.com/upload_test_1471942404278_882825266.webp");
//                showImage("https://www.baidu.com/img/bd_logo1.png");
//                showImage("http://image5.tuku.cn/wallpaper/Auto%20Wallpapers/3744_2560x1600.jpg");
                showImage("http://s1.yy.com/guild/ttime/svga/210046.svga?md5=c5e6fa4721e9bc4c8c5df2958c1f7c81");
                break;
            case R.id.btn_rose:
                showImage("http://s1.yy.com/guild/ttime/svga/210033.svga?md5=f173b738b2c06c374cc8e28527f8d676");
                break;
            case R.id.btn_angel:
                showImage("http://s1.yy.com/guild/ttime/svga/210020.svga?md5=675359bc55fa606d2d5fca3a288799f7");
                break;
            case R.id.btn_cupid1:
                showImage("http://s1.yy.com/guild/ttime/svga/210011.svga?md5=c97531a624abd0dc3dd077a94c133703");
                break;
            case R.id.btn_cupid2:
                showImage("http://s1.yy.com/guild/ttime/svga/210011_1.svga?md5=e16f106a85ee8f11bd60081274fd57f3");
                break;
            case R.id.btn_stop:
                mShowImage.getController().getAnimatable().stop();
                break;
            default:
                break;
        }
    }

    public void showImage(String uri) {
        DraweeController c = Fresco.newDraweeControllerBuilder()
                .setUri(uri)
                .setAutoPlayAnimations(true)
                .build();
        mShowImage.setController(c);
    }
}
