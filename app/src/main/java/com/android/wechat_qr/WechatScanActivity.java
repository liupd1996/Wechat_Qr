package com.android.wechat_qr;

import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.wechat_qrcode.WeChatQRCode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WechatScanActivity extends CameraActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = "WechatScanActivity";

    private CameraBridgeViewBase mOpenCvCameraView;


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };


    WeChatQRCode weChatQRCode = null;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_wechat_scan);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.fd_activity_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        weChatQRCode = WechatQr.init(this);
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(mOpenCvCameraView);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

//    private Mat                    mRgba;
//    private Mat                    mGray;

    public void onCameraViewStarted(int width, int height) {
//        mGray = new Mat();
//        mRgba = new Mat();
    }

    public void onCameraViewStopped() {
//        mGray.release();
//        mRgba.release();
    }

    List<Mat> points = new ArrayList<>();
    Scalar scalar = new Scalar(255, 255, 0, 0);
    Point center = new Point();

    Mat dstRgb = null;
    Mat dstGray = null;

    Mat m = null;

    Size size = null;
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
//        mRgba = inputFrame.rgba();
//        mGray = inputFrame.gray();
        points.clear();
        if (null != dstGray) {
            dstGray.release();
        }
        if (null != dstRgb) {
            dstRgb.release();
        }
        Mat rgba = inputFrame.rgba();//?????????
        Mat grayMat = inputFrame.gray();//????????????
        center.x = rgba.cols() / 2;
        center.y = rgba.rows() / 2;
        if (null == dstRgb) {
            m = Imgproc.getRotationMatrix2D(center, 270, 1);
            //???????????????????????????????????????????????????dstRgb???????????????????????????????????????????????????????????????dstGray
            dstRgb = new Mat(rgba.cols(), rgba.rows(), rgba.type());//???????????????????????????
            dstGray = new Mat(rgba.cols(), rgba.rows(), rgba.type());//???????????????????????????
            size = new Size(rgba.cols(), rgba.rows());
        }
        //???????????????????????????????????????????????????dstRgb???????????????????????????????????????????????????????????????dstGray
        //???????????????
        Imgproc.warpAffine(rgba, dstRgb, m, size);
        //???????????????
        Imgproc.warpAffine(grayMat, dstGray, m, size);
        List<String> results = weChatQRCode.detectAndDecode(dstRgb, points);//?????????????????????
        //?????????Opencv????????????
//        List<String> results = weChatQRCode.detectAndDecode(dstRgb, points);//?????????????????????
        if (null != results && results.size() > 0) {
            Log.e(TAG, "????????????????????????" + results.size());
            for (int i = 0, isize = results.size(); i < isize; i++) {
                Rect rect = Imgproc.boundingRect(points.get(i));
                Imgproc.rectangle(dstRgb, rect, scalar, 5);
                Imgproc.putText(dstRgb, results.get(i), rect.tl(), 0, 1, scalar);
            }
        }
        //??????????????????????????????
        return dstRgb;
    }
}