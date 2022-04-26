package com.radware.vdirect.defensepro

import com.radware.alteon.workflow.impl.DeviceConnection
import com.radware.defensepro.v8.beans.RsBWMPhysicalPortGroupEntry
import com.radware.defensepro.v8.beans.RsIDSNewRulesEntry
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class SyncPolicyPortGroup {


    public static final Logger log = LoggerFactory.getLogger(CopyConfig.class)


    public static syncPolicyPortGroups(DeviceConnection sourceDevice, List<DeviceConnection> destinationDevices,
                                       String[] networkPoliciesExecptions){
        DiffFunctions diffFunctions = new DiffFunctions()
                /*
        sync RsIDSNewRulesEntry physical port groups bean
        * */
        boolean foundMatchingBean

        destinationDevices.each{ dp ->
            RsIDSNewRulesEntry srcPolicy = new RsIDSNewRulesEntry()
            List<RsIDSNewRulesEntry> srcPolicyBeans = (List<RsIDSNewRulesEntry>) sourceDevice.readAll(srcPolicy)
            RsIDSNewRulesEntry dstPolicy = new RsIDSNewRulesEntry()

            List<RsIDSNewRulesEntry> matchedDstPolicyBeans = []
            List<RsIDSNewRulesEntry> dstPolicyBeans = (List<RsIDSNewRulesEntry>) dp.readAll(dstPolicy)

            if (!srcPolicyBeans.isEmpty()) {
                log.info ("started policy port group sync")

                if (dstPolicyBeans.isEmpty()) {
                    log.info String.format("destination DP %s network policy is empty", dp.getManagementIp())
                } else {
                    for (RsIDSNewRulesEntry srcBean : srcPolicyBeans) {
                        if(!networkPoliciesExecptions.contains(srcBean.name.toString())){
                            foundMatchingBean = false
                            for (RsIDSNewRulesEntry dstBean : dstPolicyBeans) {
                                String diffed = diffFunctions.diffPolicyPhysicalPortGroups(srcBean, dstBean)
                                if (diffed == "matched") {
                                    log.debug String.format("bean src: %s matched dst: %s", srcBean, dstBean)
                                    foundMatchingBean = true
                                    matchedDstPolicyBeans.add(dstBean)
                                    break
                                } else if (diffed == "update") {
                                    foundMatchingBean = true
                                    matchedDstPolicyBeans.add(dstBean)
                                    log.info String.format("bean src: %s needs to be updated dst: %s", srcBean, dstBean)
                                    dstBean.setPortmask(srcBean.portmask)
                                    dp.update(dstBean)
                                    break
                                }
                            }
                            if (!foundMatchingBean) {
                                log.debug String.format("dest device has no bean %s", srcBean.getName())
                                //dp.create(srcBean)
                            }
                        }
                    }
                }
            }
        }
    }
}
