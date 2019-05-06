package com.example.app.mogo.activity.passageiro;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;

import com.example.app.mogo.R;
import com.squareup.picasso.Picasso;

public class HistoricoActivity extends AppCompatActivity {

    private ImageView imageMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico);

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imageMap = findViewById(R.id.imageMap);

        String lat = "-12.958811";
        String lon = "-38.401606";

        String url ="https://maps.googleapis.com/maps/api/staticmap?&zoom=14&size=330x130&maptype=roadmap&markers=color:green%7Clabel:G%7C-12.958811,-38.401606&key=AIzaSyCK6C5R89CQGwuW8ICRn37IKp_FGKwmtXk";
        /*url+="&zoom=14";
        url+="&size=330x130";
        url+="&maptype=roadmap";
        url+="&markers=color:green%7Clabel:G%7C"+lat+","+lon;
        url+="&key=AIzaSyCK6C5R89CQGwuW8ICRn37IKp_FGKwmtXk";*/

        Picasso.get().load(url).into(imageMap);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // ação voltar do action bar home/up
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
