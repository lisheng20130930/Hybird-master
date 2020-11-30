LOCAL_PATH := $(call my-dir)


#qpBase
include $(CLEAR_VARS)
LOCAL_MODULE    := qpBase
LOCAL_ARM_MODE  := arm
LOCAL_CFLAGS 	:= -D__ANDROID__ -frtti -fexceptions -std=c99
LOCAL_LDLIBS 	:= -ldl -landroid -lz -Wl,-E

LOCAL_C_INCLUDES:=\
	$(LOCAL_PATH)/../../classes \
	$(LOCAL_PATH)/../../utils \
	$(LOCAL_PATH)/../../evnet

LOCAL_SRC_FILES := init.c \
  JNIHelper.c\
	../../evnet/acceptor.c\
	../../evnet/aesocket.c\
	../../evnet/channel.c\
	../../evnet/dataqueue.c\
	../../evnet/event.c\
	../../evnet/evpipe.c\
	../../evnet/libnet.c\
	../../evnet/httpc.c\
	../../evnet/httparser.c\
	../../utils/buffer.c\
	../../utils/libos.c\
	../../utils/log.c\
	../../classes/assetMgr.c \
	../../classes/env.c\
	../../classes/utils.c
	
include $(BUILD_SHARED_LIBRARY)
