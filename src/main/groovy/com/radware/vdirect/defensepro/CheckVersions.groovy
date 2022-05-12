package com.radware.vdirect.defensepro

import com.radware.alteon.api.AdcRuntimeException
import com.radware.alteon.workflow.impl.DeviceConnection
import com.radware.defensepro.v8.beans.RndDeviceParams
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class CheckVersions {
    public static final SUPPORTED_VERSIONS = [8]
    private static final Logger log = LoggerFactory.getLogger(CheckVersions.class)

    public static compare(DeviceConnection sourceDevice ,List<DeviceConnection> destinationDevices){
        List<DeviceConnection> allDevices = new ArrayList<>()
        String deviceVersion
        Integer majorVer

        allDevices.add(sourceDevice)
        destinationDevices.each{ device ->
            if(!(allDevices.contains(device))){
                allDevices.add(device)
            }
        }

        allDevices.each { device ->
            RndDeviceParams rndDeviceParams = new RndDeviceParams()
            RndDeviceParams versionBean = (RndDeviceParams) device.read(rndDeviceParams)
            if (deviceVersion == null){
                deviceVersion = versionBean.rndBrgVersion
            }else{
                if (deviceVersion != versionBean.rndBrgVersion){
                    throw new AdcRuntimeException("device versions is different, please use the same version for all devices")
                }
            }
            String[] ver_split = deviceVersion.split("[.]")
            majorVer = ver_split[0] as Integer
            if (!SUPPORTED_VERSIONS.contains(majorVer)){
                throw new AdcRuntimeException("version is not supported")
            }
        }
        return majorVer
    }
}