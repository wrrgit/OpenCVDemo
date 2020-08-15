package com.wqx.opencv;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

/*
功能介绍：深入OpenCV Android应用开发第二章代码，检测图像的基本特征
        包括了Canny边缘检测法Sobel边缘检测法等
实现步骤：1.从手机中取出一张图片作为原始图片,通过点击menu对应的按钮开始选择图片
        2.通过menu按钮选择要对照片进行的图像处理
 */
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private final static int CANNY = 0;
    private final static int HARRIS = 1;
    private final static int HOUGH = 2;
    private final static int HOUGH2 = 3;
    private final static String TAG = "infor";

    private Mat src = null;//定义一个Mat型类用于临时存放选择的图片
    private Mat image = null;//用于存放得到的图片
    private Mat des = null;//用于临时存放Mat型类的图片
    private Bitmap resultBitmap;
    private ImageView pictureView = null;//定义一个ImageView类视图用于存放选择的图片

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {

            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    /*在这里执行自己的语句*/
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pictureView = (ImageView) findViewById(R.id.Picture);

    }

    /*启动openCV*/
    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /*在这里选取要进行的操作*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //对应Canny边缘检测的按钮
        if (id == R.id.Canny) {
            /*下面对通过Intent对象得到选择图片的Activity，最后返回图片的信息，得到图片*/
            Intent pictureSelectIntent = new Intent(Intent.ACTION_PICK);//设置Action
            pictureSelectIntent.setType("image/");//设置数据的类型
            startActivityForResult(pictureSelectIntent, CANNY);
            return true;
        }

        //对应Harris边缘检测的按钮
        if (R.id.Harris == id) {
            Intent pictureSelectIntent = new Intent(Intent.ACTION_PICK);
            pictureSelectIntent.setType("image/");
            startActivityForResult(pictureSelectIntent, HARRIS);
            return true;
        }
        //对应Hough的直线检测按钮
        if (R.id.Hough == id) {
            Intent pictureSelectIntent = new Intent(Intent.ACTION_PICK);
            pictureSelectIntent.setType("image/");
            startActivityForResult(pictureSelectIntent, HOUGH);
            return true;
        }
        //对应Hough的直线检测按钮
        if (R.id.Hough2 == id) {
            Intent pictureSelectIntent = new Intent(Intent.ACTION_PICK);
            pictureSelectIntent.setType("image/");
            startActivityForResult(pictureSelectIntent, HOUGH2);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /*调用StartActivityForResult后的回调函数
     * 在这个函数里面得到图片然后进行相应的处理
     * */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RESULT_OK == resultCode) {
            switch (requestCode) {
                case CANNY:
                    try {
                        image = getPicture(data);
                        Toast.makeText(MainActivity.this, "图片选取成功", Toast.LENGTH_SHORT).show();
                        resultBitmap = canny(image);
                        pictureView.setImageBitmap(resultBitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
                case HARRIS:
                    try {
                        image = getPicture(data);//得到图片
                        Toast.makeText(MainActivity.this, "图片选取成功", Toast.LENGTH_SHORT).show();
                        resultBitmap = harris(image);//角点检测的图像处理
                        pictureView.setImageBitmap(resultBitmap);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
                case HOUGH:
                    try {
                        image = getPicture(data);//得到图片
                        Toast.makeText(MainActivity.this, "图片选取成功", Toast.LENGTH_SHORT).show();
                        resultBitmap = houghLine(image);
                        pictureView.setImageBitmap(resultBitmap);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
                case HOUGH2:
                    try {
                        image = getPicture(data);//得到图片
                        Toast.makeText(MainActivity.this, "图片选取成功", Toast.LENGTH_SHORT).show();
                        resultBitmap = MyHoughLine2(image);
                        pictureView.setImageBitmap(resultBitmap);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    /*得到图片*/
    public Mat getPicture(Intent data) throws FileNotFoundException {
        /*下面的代码是获得手机内的图片*/
        final Uri imageUri = data.getData();//得到图片的路径
        final InputStream imageStream = getContentResolver().openInputStream(imageUri);//得到基于路径的流文件
        final Bitmap selectImage = BitmapFactory.decodeStream(imageStream);//得到了图片的位图

        /*下面将位图转换成Mat型，可以进行图片的处理*/
        src = new Mat(selectImage.getHeight(), selectImage.getWidth(), CvType.CV_8UC4);
        Utils.bitmapToMat(selectImage, src);

        return src;
    }

    /* 下面进行图片的处理
     *
     * Canny边缘处理
     */
    public Bitmap canny(Mat src) {
        Bitmap result;
        Mat grayMat = new Mat();
        Mat cannyEdges = new Mat();
        /*将图片转换成灰度图*/
        Imgproc.cvtColor(src, grayMat, Imgproc.COLOR_BGR2GRAY);
        /*得到边缘图,这里最后两个参数控制着选择边缘的阀值上限和下限*/
        Imgproc.Canny(grayMat, cannyEdges, 50, 300);
        /*将Mat图转换成位图*/
        result = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(cannyEdges, result);
        return result;
    }

    /*Harris角点检测*/
    public Bitmap harris(Mat src) {
        Bitmap resultHarris;
        Mat grayMat = new Mat();
        Mat corners = new Mat();
        /*将图片转换成灰度图*/
        Imgproc.cvtColor(src, grayMat, Imgproc.COLOR_BGR2GRAY);
        /*找出角点*/
        Mat tempDst = new Mat();
        Imgproc.cornerHarris(grayMat, tempDst, 2, 3, 0.04);
        /*归一化Harris角点的输出*/
        Mat tempDstNorm = new Mat();
        Core.normalize(tempDst, tempDstNorm, 0, 255, Core.NORM_MINMAX);
        Core.convertScaleAbs(tempDstNorm, corners);
        /*在新的图片上绘制角点*/
        Random r = new Random();
        for (int i = 0; i < tempDstNorm.cols(); i++) {
            for (int j = 0; j < tempDstNorm.rows(); j++) {
                double[] value = tempDstNorm.get(j, i);
                if (value[0] > 250) {//决定了画出哪些角点，值越大选择画出的点就越少。如果程序跑的比较慢，就是由于值选取的太小，导致画的点过多
                    Imgproc.circle(corners, new Point(i, j), 5, new Scalar(r.nextInt(255)), 2);
                }
            }
        }
        /*将Mat图转换成位图*/
        resultHarris = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);//这一步至关重要，必须初始化Bitmap对象的大小
        Utils.matToBitmap(corners, resultHarris);
        return resultHarris;
    }

    /*Hough直线检测*/
    public Bitmap houghLine(Mat src) {
        Bitmap resultHough;
        Mat grayMat = new Mat();
        Mat cannyEdges = new Mat();
        Mat lines = new Mat();
        Mat origination = new Mat(src.size(), CvType.CV_8UC4);
        src.copyTo(origination);//拷贝
        /*通过Canny得到边缘图*/
        Imgproc.cvtColor(origination, grayMat, Imgproc.COLOR_BGR2GRAY);//灰度图片
        //Imgproc.Canny(grayMat, cannyEdges, 50, 300);
        Imgproc.Canny(grayMat, cannyEdges, 50, 150, 3);
        //Mat cannyEdges = new Mat(resultHough.getHeight(),resultHough.getWidth(),CvType.CV_8UC1);
        /*获得直线图*/
        //Imgproc.HoughLinesP(cannyEdges, lines, 1, Math.PI / 180, 10, 0, 50);
        //maxLineGap 点的间隔
        //minLineLength 最小线长
        Imgproc.HoughLinesP(cannyEdges, lines, 1, Math.PI / 180, 100, 0, 100);
        Mat houghLines = new Mat();
        houghLines.create(cannyEdges.rows(), cannyEdges.cols(), CvType.CV_8UC3);//背景色   CvType.CV_8UC4 白底，CV_8UC1 黑底，CV_8UC3 直线的颜色才起作用
        
        /*在图线的上绘制直线*/
        for (int i = 0; i < lines.rows(); i++) {
            double[] points = lines.get(i, 0);
            if (null != points) {
                double x1, y1, x2, y2;
                x1 = points[0];
                y1 = points[1];
                x2 = points[2];
                y2 = points[3];
                Point pt1 = new Point(x1, y1);
                Point pt2 = new Point(x2, y2);
                /*在一幅图像上绘制直线*/
                //Imgproc.line(houghLines, pt1, pt2, new Scalar(55, 100, 195), 3);// thickness  画线的宽度
                Imgproc.line(houghLines, pt1, pt2, new Scalar(255, 255, 0), 3);// thickness  画线的宽度
            }
        }
        resultHough = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(houghLines, resultHough);
        return resultHough;
    }

    /*Hough直线检测*/
    public Bitmap MyHoughLine2(Mat src) {
//        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
//        // Reading the Image from the file and storing it in to a Matrix object
//        String file = "F:/worksp/opencv/images/hough_input.jpg";
//        // Reading the image
//        Mat src = Imgcodecs.imread(file,0);
//        商业请保留原文链接：https://www.yiibai.com/opencv/opencv_hough_line_transform.html
        // Detecting edges of it
        Mat canny = new Mat();
        Imgproc.Canny(src, canny, 50, 200, 3, false);
        Bitmap resultHough;
        // Changing the color of the canny
        Mat cannyColor = new Mat();
        Imgproc.cvtColor(canny, cannyColor, Imgproc.COLOR_GRAY2BGR);
        // Detecting the hough lines from (canny)
        Mat lines = new Mat();
        Imgproc.HoughLines(canny, lines, 1, Math.PI / 180, 100);
        // Drawing lines on the image
        double[] data;
        double rho, theta;
        Point pt1 = new Point();
        Point pt2 = new Point();
        double a, b;
        double x0, y0;
        for (int i = 0; i < lines.cols(); i++) {
            data = lines.get(0, i);
            rho = data[0];
            theta = data[1];
            a = Math.cos(theta);
            b = Math.sin(theta);
            x0 = a * rho;
            y0 = b * rho;
            pt1.x = Math.round(x0 + 1000 * (-b));
            pt1.y = Math.round(y0 + 1000 * (a));
            pt2.x = Math.round(x0 - 1000 * (-b));
            pt2.y = Math.round(y0 - 1000 * (a));
            Imgproc.line(cannyColor, pt1, pt2, new Scalar(0, 100, 255), 6);
        }
        // Writing the image
        //Imgcodecs.imwrite("F:/worksp/opencv/images/hough_output.jpg", cannyColor);//原文出自【易百教程】，商业转载请联系作者获得授权，非商业请保留原文链接：https://www.yiibai.com/opencv/opencv_hough_line_transform.html
        resultHough = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(canny, resultHough);
        return resultHough;
    }
}