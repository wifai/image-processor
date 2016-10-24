package ga.wiwit.imageprocessor;

import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;

public class MainActivity extends AppCompatActivity {
    private ImageView imageView;
    private ImageView histogramImage;
    private ImageContainer imageContainer;
    private Button load, reset, filter, binarization, noise_reducer, sceletonization;
    private SeekBar seekBar1, seekBar2, seekBar3;
    private HistogramVisual histogramVisual;
    private Spinner spinner;
    private int selected = -1;
    private int[][][] matrix_convs;
    private int[] freq_mat;
    private int[] norm_mat;

    int RESULT_LOAD_IMAGE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView)findViewById(R.id.image);
        load = (Button)findViewById(R.id.load);
        reset = (Button)findViewById(R.id.reset);
        filter = (Button)findViewById(R.id.filter);
        binarization = (Button)findViewById(R.id.binarization);
        noise_reducer = (Button)findViewById(R.id.noise_reducer);
        sceletonization = (Button)findViewById(R.id.sceletonization);
        binarization = (Button)findViewById(R.id.binarization);
        histogramImage = (ImageView)findViewById((R.id.histogram));
        seekBar1 = (SeekBar) findViewById(R.id.seek_bar1);
        seekBar2 = (SeekBar) findViewById(R.id.seek_bar2);
        seekBar3 = (SeekBar) findViewById(R.id.seek_bar3);
        imageContainer = new ImageContainer();
        histogramVisual = new HistogramVisual();
        spinner = (Spinner) findViewById(R.id.spinner);
        final Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.matrix_convolution_name, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
        selected = -1;

        Resources res = getResources();
        TypedArray ta = res.obtainTypedArray(R.array.matrix_convolution);
        int n = ta.length();
        matrix_convs = new int[n][3][3];
        freq_mat = new int[n];
        norm_mat = new int[n];
        for (int i = 0; i < n; ++i) {
            int id = ta.getResourceId(i, 0);
            if (id > 0) {
                int[] now = res.getIntArray(id);
                freq_mat[i] = now[0];
                norm_mat[i] = now[9];
                for (int j = 0; j < 9; j++) {
                    matrix_convs[i][j/3][j % 3] = now[j+1];
                }
            } else {
                // something wrong with the XML
                assert(false);
            }
        }
        ta.recycle(); // Important!

        load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                i.setType("image/*");
                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setImageBitmap(imageContainer.reset());
                histogramVisual.setOriginHistogram(imageContainer.getHistogramGrayScale());
                histogramImage.setImageBitmap(histogramVisual.getBitmap());
            }
        });
        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selected = spinner.getSelectedItemPosition();
                if (selected < 0)
                    return;
                imageContainer.applyConvolutionMatrix(freq_mat[selected], matrix_convs[selected], norm_mat[selected]);
                imageView.setImageBitmap(imageContainer.getBitmap());
                histogramVisual.setOriginHistogram(imageContainer.getHistogramGrayScale());
                histogramImage.setImageBitmap(histogramVisual.getBitmap());
            }
        });

        binarization.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageContainer.toBinary();
                imageView.setImageBitmap(imageContainer.getBitmap());
                histogramVisual.setOriginHistogram(imageContainer.getHistogramGrayScale());
                histogramImage.setImageBitmap(histogramVisual.getBitmap());
            }
        });
        noise_reducer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageContainer.reduceNoise();
                imageView.setImageBitmap(imageContainer.getBitmap());
                histogramVisual.setOriginHistogram(imageContainer.getHistogramGrayScale());
                histogramImage.setImageBitmap(histogramVisual.getBitmap());
            }
        });

        sceletonization.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageContainer.zhangSuen();
                imageView.setImageBitmap(imageContainer.getBitmap());
                histogramVisual.setOriginHistogram(imageContainer.getHistogramGrayScale());
                histogramImage.setImageBitmap(histogramVisual.getBitmap());
            }
        });
        seekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int result = seekBar.getProgress() * histogramVisual.getWidth() / 101;
                histogramVisual.setUp(result);
                HistogramSpecification histogramSpecification = new HistogramSpecification(
                        histogramVisual.getOriginHistogram(),
                        histogramVisual.getExpectedHistogram());
                int[] lookUp = histogramSpecification.getLookUp();
                histogramVisual.calculateActualHistogram(lookUp);
                histogramImage.setImageBitmap(histogramVisual.getBitmap());
                imageView.setImageBitmap(imageContainer.applyLookUpAll(lookUp));
            }
        });
        seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int result = seekBar.getProgress() * histogramVisual.getHeight() / 101;
                histogramVisual.setLeft(result);
                HistogramSpecification histogramSpecification = new HistogramSpecification(
                        histogramVisual.getOriginHistogram(),
                        histogramVisual.getExpectedHistogram());
                int[] lookUp = histogramSpecification.getLookUp();
                histogramVisual.calculateActualHistogram(lookUp);
                histogramImage.setImageBitmap(histogramVisual.getBitmap());
                imageView.setImageBitmap(imageContainer.applyLookUpAll(lookUp));
            }
        });
        seekBar3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int result = seekBar.getProgress() * histogramVisual.getHeight() / 101;
                histogramVisual.setRight(result);
                HistogramSpecification histogramSpecification = new HistogramSpecification(
                        histogramVisual.getOriginHistogram(),
                        histogramVisual.getExpectedHistogram());
                int[] lookUp = histogramSpecification.getLookUp();
                histogramVisual.calculateActualHistogram(lookUp);
                histogramImage.setImageBitmap(histogramVisual.getBitmap());
                imageView.setImageBitmap(imageContainer.applyLookUpAll(lookUp));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            imageView.setImageURI(uri);
            Drawable drawable = imageView.getDrawable();
            Bitmap originBitmap = ((BitmapDrawable) drawable).getBitmap();
            Bitmap bitmap = originBitmap.copy(Bitmap.Config.ARGB_8888, true);
            imageContainer = new ImageContainer(bitmap);
            histogramVisual = new HistogramVisual(imageContainer.getHistogramGrayScale());
            histogramImage.setImageBitmap(histogramVisual.getBitmap());
        }
    }
}
