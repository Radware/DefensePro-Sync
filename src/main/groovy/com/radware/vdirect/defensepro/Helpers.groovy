package com.radware.vdirect.defensepro

import com.radware.alteon.workflow.impl.DeviceConnection
import com.radware.defensepro.v8.beans.RsBWMPhysicalPortGroupEntry
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Helpers {
    public static final Logger log = LoggerFactory.getLogger(Helpers.class)

    static syncPhysicalPortGroup(DeviceConnection sourceDevice, List<DeviceConnection> destinationDevices) {

        /*
test of sync white list bean
Read All
* */
        DiffFunctions diffFunctions = new DiffFunctions()
        boolean foundMatchingBean

        RsBWMPhysicalPortGroupEntry srcPhysicalPortGroupEntry = new RsBWMPhysicalPortGroupEntry()
        List<RsBWMPhysicalPortGroupEntry> srcPhysicalPortGroupBeans = (List<RsBWMPhysicalPortGroupEntry>) sourceDevice.readAll(srcPhysicalPortGroupEntry)
        RsBWMPhysicalPortGroupEntry dstPhysicalPortGroupEntry = new RsBWMPhysicalPortGroupEntry()

        destinationDevices.each { dp ->
            //log.info("started app port sync + black white lists")
            log.info String.format("working on dp %s", dp.getManagementIp())

            /*
        sync Physical Port Group bean
        * */

            List<RsBWMPhysicalPortGroupEntry> beansToDeletePhysicalPortGroup = []
            List<RsBWMPhysicalPortGroupEntry> matchedDstPhysicalPortGroupBeans = []
            List<RsBWMPhysicalPortGroupEntry> dstPhysicalPortGroupBeans = (List<RsBWMPhysicalPortGroupEntry>) dp.readAll(dstPhysicalPortGroupEntry)

            if (!srcPhysicalPortGroupBeans.isEmpty()) {
                log.info("started physical port group sync")

                if (dstPhysicalPortGroupBeans.isEmpty()) {
                    log.info String.format("destination DP %s App port Group is empty", dp.getManagementIp())
                    for (RsBWMPhysicalPortGroupEntry srcBean : srcPhysicalPortGroupBeans) {
                        dp.create(srcBean)
                    }
                } else {
                    for (RsBWMPhysicalPortGroupEntry srcBean : srcPhysicalPortGroupBeans) {
                        foundMatchingBean = false
                        for (RsBWMPhysicalPortGroupEntry dstBean : dstPhysicalPortGroupBeans) {
                            String diffed = diffFunctions.diffPhysicalPortGroups(srcBean, dstBean)
                            if (diffed == "matched") {
                                log.debug String.format("bean src: %s matched dst: %s", srcBean, dstBean)
                                foundMatchingBean = true
                                matchedDstPhysicalPortGroupBeans.add(dstBean)
                                break
                            } else if (diffed == "update") {
                                foundMatchingBean = true
                                matchedDstPhysicalPortGroupBeans.add(dstBean)
                                log.debug String.format("bean src: %s needs to be updated dst: %s", srcBean, dstBean)
                                dp.update(srcBean)
                                break
                            }
                        }
                        if (!foundMatchingBean) {
                            log.debug String.format("dest device has no bean %s", srcBean.getName())
                            dp.create(srcBean)
                        }
                    }
                }
//                if (matchedDstPhysicalPortGroupBeans && deleteUnusedPolicies){
//                    log.info("delete of Physical Port Group bean started")
//                    deleteSparePolicies(matchedDstPhysicalPortGroupBeans, dstPhysicalPortGroupEntry, beansToDeletePhysicalPortGroup, dp)
//                }
            }
        }

    }
}
