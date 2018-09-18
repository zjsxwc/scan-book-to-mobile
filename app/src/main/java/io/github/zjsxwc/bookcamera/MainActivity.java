package io.github.zjsxwc.bookcamera;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class MainActivity extends AppCompatActivity {

    public static final int TAKE_PHOTO = 1;
    private Uri imageUri;


    private void initPath() {
        File dir = new File(Environment.getExternalStorageDirectory(),
                "/" + getResources().getString(R.string.app_name));

        if (!dir.exists()) {
            dir.mkdirs();
        }

        if (!dir.exists()) {
            Toast.makeText(MainActivity.this, "安卓版本太高，没有读写文件权限啊，请去应用设置权限", Toast.LENGTH_LONG).show();
        }

    }


    private String getConfig(String key) {
        String value = "";
        Properties properties = new Properties();
        try {
            FileInputStream fis = new FileInputStream(new File(getCacheDir(), "config"));
            properties.load(fis);
            value = properties.getProperty(key);

            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    private void setConfig(String key, String value) {
        Properties properties = new Properties();
        try {
            FileOutputStream fos = new FileOutputStream(new File(getCacheDir(), "config"));
            properties.setProperty(key, value);
            properties.store(fos, null);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void incNumber() {
        String numberValue = getConfig(NUMBER_KEY);
        int value = 1;
        if (numberValue.length() > 0) {
            value = Integer.valueOf(numberValue);
        }
        value++;
        setConfig(NUMBER_KEY, String.valueOf(value));
        number.setText(String.valueOf(value));
    }


    public static final String NUMBER_KEY = "number";
    private EditText number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("标题");
        setSupportActionBar(toolbar);

        initPath();


        number = findViewById(R.id.number);
        String numberValue = getConfig(NUMBER_KEY);
        if (numberValue.length() > 0) {
            number.setText(numberValue);
        } else {
            number.setText("1");
            setConfig(NUMBER_KEY, "1");
        }
        number.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.i("Ansen", "内容改变之后调用:" + s);

                setConfig(NUMBER_KEY, s.toString());
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File outputImage = new File(Environment.getExternalStorageDirectory(),
                        "/" + getResources().getString(R.string.app_name) + "/tempImage" + ".jpg");

                try {
                    if (outputImage.exists()) {
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                imageUri = Uri.fromFile(outputImage);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, TAKE_PHOTO);
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            String filename = "/" + getResources().getString(R.string.app_name) + "/" + getConfig(NUMBER_KEY) + ".jpg";
            saveBitmapAsJpg(imageUri, filename);
            Toast.makeText(this, "Saved " + filename, Toast.LENGTH_SHORT).show();
            incNumber();
        }

    }


    public void saveBitmapAsJpg(Uri imageUri, String filename) {

        try {
            Bitmap bmp = BitmapFactory.decodeStream(getContentResolver()
                    .openInputStream(imageUri));
            Bitmap grayBmp = toGrayScale(bmp);

            File f = new File(Environment.getExternalStorageDirectory(),
                    filename);

            try {
                if (f.exists()) {
                    f.delete();
                }
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }


            FileOutputStream out = new FileOutputStream(f);
            grayBmp.compress(Bitmap.CompressFormat.JPEG, 20, out);

            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public Bitmap toGrayScale(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
