package canvasanimation.example.cocoonshu.com.canvasanimation;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private String               mStrFilePath  = null;
    private FloatingActionButton mFabPickFile  = null;
    private TextView             mTxvImagePath = null;
    private AvatarView           mArvImage     = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeIntent();
        initializeActionBar();
        initializeViews();
        initializeListeners();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initializeIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.getData() != null) {
            mStrFilePath = Uri.decode(intent.getDataString());
        }
    }

    private void initializeActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void initializeViews() {
        mFabPickFile  = (FloatingActionButton) findViewById(R.id.fab);
        mTxvImagePath = (TextView) findViewById(R.id.TextView_ImagePath);
        mArvImage     = (AvatarView) findViewById(R.id.AvatarView_Image);
    }

    private void initializeListeners() {
        mFabPickFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startImagePicker();
            }
        });
    }

    private void startImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        startActivityForResult(Intent.createChooser(intent, "测试图片"), 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null && data.getData() != null) {
            mStrFilePath = getPath(MainActivity.this, data.getData());
            mTxvImagePath.setText(getResources().getString(R.string.MainActivity_FilePath) + mStrFilePath);
            new BitmapLoader().execute(mStrFilePath);
        }
    }

    public static String getPath(Context context, Uri uri) {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { MediaStore.Images.Media.DATA };
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection,null, null, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    private class BitmapLoader extends AsyncTask<String, Integer, Bitmap> {

        private final static int LimitSize = 1920;

        @Override
        protected Bitmap doInBackground(String... params) {
            String                imagePath  = (String)params[0];
            int                   sampleSize = 1;
            BitmapFactory.Options options    = new BitmapFactory.Options();

            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imagePath);
            sampleSize = Math.max(options.outWidth, options.outHeight) / LimitSize;
            sampleSize = sampleSize < 1 ? 1 : sampleSize;

            options.inJustDecodeBounds = false;
            options.inSampleSize = sampleSize;
            return BitmapFactory.decodeFile(imagePath);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                mArvImage.setAvatarImage(bitmap);
            } else {
                mTxvImagePath.setText(mTxvImagePath.getText() + " decode failed!");
            }
        }
    }
}
