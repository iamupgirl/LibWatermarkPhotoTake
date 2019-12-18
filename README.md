# LibWatermarkPhotoTake
一个简单的水印拍照，加上拍照时间和经纬度水印，支持横竖拍适配

调用拍照或相册使用如下方法：
count(int maxsize) // 最大相片选择或拍照的数量
origin(String filePath) // 存放路径
latLon(String locationStr) // 水印（经纬度）
	
MultiImageSelector.create(mContext)
                .count(3)
                .origin(adapterImgPath)
                .latLon(str)
                .start(mActivity, REQUEST_IMAGE);

// 拍照返回回调
@Override
public void onActivityResult(int requestCode, int resultCode, Intent data) {
	if (requestCode == REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {
                // 获取返回的图片列表
                mPhotoPath = data.getStringArrayListExtr(MultiImageSelectorActivity.EXTRA_RESULT);
		}
	}
}
        
