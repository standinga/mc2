#include <jni.h>
#include <android/log.h>
#include <time.h>
//#include <android/bitmap.h>

#define LOGTAG "Mirror_Native"

//inline int32_t toInt(jbyte pValue) {
//	return (0xff & (int32_t) pValue);
//}
//
//inline int32_t max(int32_t pValue1, int32_t pValue2) {
//	if (pValue1 < pValue2) {
//		return pValue2;
//	} else {
//		return pValue1;
//	}
//}
//
//inline int32_t clamp(int32_t pValue, int32_t pLowest,
//					 int32_t pHighest) {
//	if (pValue < 0) {
//		return pLowest;
//	} else if (pValue > pHighest) {
//		return pHighest;
//	} else {
//		return pValue;
//	}
//}
//
//inline int32_t color(pColorR, pColorG, pColorB) {
//	return 0xFF000000 | ((pColorB << 6)  & 0x00FF0000)
//		   | ((pColorG >> 2)  & 0x0000FF00)
//		   | ((pColorR >> 10) & 0x000000FF);
//}
//
//JNIEXPORT void JNICALL Java_co_borama_mirrorCoach_OverlayView_doNativeProcessing
//		(JNIEnv * pEnv, jclass pClass, jobject pTarget, jbyteArray pSource){
//
//
//	// Retrieves bitmap information and locks it for drawing.
//	AndroidBitmapInfo lBitmapInfo;
//	if (AndroidBitmap_getInfo(pEnv, pTarget, &lBitmapInfo) < 0) {
//		return;
//	}
//	if (lBitmapInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
//		return;
//	}
//
//	uint32_t* lBitmapContent;
//	if (AndroidBitmap_lockPixels(pEnv, pTarget,
//								 (void**)&lBitmapContent) < 0) {
//		return;
//	}
//
//	// Accesses source array data.
//	jbyte* lSource = (*pEnv)->GetPrimitiveArrayCritical(pEnv,
//														pSource, 0);
//	if (lSource == NULL) {
//		return;
//	}
//
//	int32_t lFrameSize = lBitmapInfo.width * lBitmapInfo.height;
//	int32_t lYIndex, lUVIndex;
//	int32_t lX, lY;
//	int32_t lColorY, lColorU, lColorV;
//	int32_t lColorR, lColorG, lColorB;
//	int32_t y1192;
//
//	// Processes each pixel and converts YUV to RGB color.
//	// Algorithm originating from Ketai open source project.
//	// See http://ketai.googlecode.com/.
//	for (lY = 0, lYIndex = 0; lY < lBitmapInfo.height; ++lY) {
//		lColorU = 0; lColorV = 0;
//		// Y is divided by 2 because UVs are subsampled vertically.
//		// This means that two consecutives iterations refer to the
//		// same UV line (e.g when Y=0 and Y=1).
//		lUVIndex = lFrameSize + (lY >> 1) * lBitmapInfo.width;
//
//		for (lX = 0; lX < lBitmapInfo.width; ++lX, ++lYIndex) {
//			// Retrieves YUV components. UVs are subsampled
//			// horizontally too, hence %2 (1 UV for 2 Y).
//			lColorY = max(toInt(lSource[lYIndex]) - 16, 0);
//			if (!(lX % 2)) {
//				lColorV = toInt(lSource[lUVIndex++]) - 128;
//				lColorU = toInt(lSource[lUVIndex++]) - 128;
//			}
//
//			// Computes R, G and B from Y, U and V.
//			y1192 = 1192 * lColorY;
//			lColorR = (y1192 + 1634 * lColorV);
//			lColorG = (y1192 - 833  * lColorV - 400 * lColorU);
//			lColorB = (y1192 + 2066 * lColorU);
//
//			lColorR = clamp(lColorR, 0, 262143);
//			lColorG = clamp(lColorG, 0, 262143);
//			lColorB = clamp(lColorB, 0, 262143);
//
//			// Combines R, G, B and A into the final pixel color.
//			lBitmapContent[lYIndex] = color(lColorR,lColorG,lColorB);
//		}
//	}
//
//	// Unlocks the bitmap and releases the Java array when finished.
//	(*pEnv)-> ReleasePrimitiveArrayCritical(pEnv,pSource,lSource, 0);
//	AndroidBitmap_unlockPixels(pEnv, pTarget);
//}
//


JNIEXPORT void JNICALL Java_co_borama_mirrorCoach_OverlayView_doNativeProcessing(
		JNIEnv *env, jobject this, jbyteArray yuv420sp, jintArray rgb,jint width, jint height, jint jflipped) {

//	clock_t tic = clock();

	int             sz;
	int             i;
	int             j;
	int             Y;
	int             Cr = 0;
	int             Cb = 0;
	int             pixPtr = 0;
    int             flipPtr = 0;
	int             jDiv2 = 0;
	int             R = 0;
	int             G = 0;
	int             B = 0;
	int             cOff;
	int w = width;
	int h = height;
    int flipped = jflipped; // extra value to check if front or rear camera (1 - front camera - need to flip screen)
	sz = w * h;


	jint *rgbData = (jint*) ((*env)->GetPrimitiveArrayCritical(env, rgb, 0));
	jbyte* yuv = (jbyte*) (*env)->GetPrimitiveArrayCritical(env, yuv420sp, 0);


	for(j = 0; j < h; j++) {
		pixPtr = j * w ;
        flipPtr = j * w + w -1;
		jDiv2 = j >> 1;
		for(i = 0; i < w; i++) {
			Y = yuv[pixPtr];

			if(Y < 0) Y += 255;
			if((i & 0x1) != 1) {
				cOff = sz + jDiv2 * w + i;
				Cb = yuv[cOff];
				if(Cb < 0) Cb += 127; else Cb -= 128;
				Cr = yuv[cOff + 1];
				if(Cr < 0) Cr += 127; else Cr -= 128;
			}
			Y = Y + (Y >> 3) + (Y >> 5) + (Y >> 7);
			R = Y + (Cr << 1) + (Cr >> 6);
			if(R < 0) R = 0; else if(R > 255) R = 255;
			G = Y - Cb + (Cb >> 3) + (Cb >> 4) - (Cr >> 1) + (Cr >> 3);
			if(G < 0) G = 0; else if(G > 255) G = 255;
			B = Y + Cb + (Cb >> 1) + (Cb >> 4) + (Cb >> 5);
			if(B < 0) B = 0; else if(B > 255) B = 255;
            if (flipped == 0)
            {rgbData[pixPtr++] = 0xff000000 + (B << 16) + (G << 8) + R;}
            else {
                rgbData[flipPtr--] = 0xff000000 + (B << 16) + (G << 8) + R;
                pixPtr++;
            }
		}
	}


	(*env)->ReleasePrimitiveArrayCritical(env, rgb, rgbData, 0);
	(*env)->ReleasePrimitiveArrayCritical(env, yuv420sp, yuv, 0);


//	clock_t toc = clock();

//	int timing = (int)((double)(toc - tic) / CLOCKS_PER_SEC * 1000000.0);

//	__android_log_print(ANDROID_LOG_VERBOSE, LOGTAG, "Elapsed: %d\n", timing); // logging

//	return (jint) timing;

}


