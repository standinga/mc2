LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

//LOCAL_LDLIBS := -llog
LOCAL_MODULE := mirrorcoach
LOCAL_SRC_FILES := native.c
LOCAL_CFLAGS += -ffast-math -O3

include $(BUILD_SHARED_LIBRARY)
