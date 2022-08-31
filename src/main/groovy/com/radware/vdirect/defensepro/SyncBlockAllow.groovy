package com.radware.vdirect.defensepro

import com.radware.alteon.workflow.impl.DeviceConnection
import com.radware.defensepro.v8.beans.RsBWMAppPortGroupEntry
import com.radware.defensepro.v8.beans.RsBWMNetworkEntry
import com.radware.defensepro.v8.beans.RsBWMPhysicalPortGroupEntry
import com.radware.defensepro.v8.beans.RsNewBlackListEntry
import com.radware.defensepro.v8.beans.RsNewWhiteListEntry
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class SyncBlockAllow {
    public static final Logger log = LoggerFactory.getLogger(SyncBlockAllow.class)

    public static syncAppPortGroup(DeviceConnection sourceDevice, List<DeviceConnection> destinationDevices,
                                   Boolean deleteUnusedPolicies){
        DiffFunctions diffFunctions = new DiffFunctions()
        boolean foundMatchingBean

        RsBWMAppPortGroupEntry srcAppPortGroupEntry = new RsBWMAppPortGroupEntry()
        List<RsBWMAppPortGroupEntry> srcAppPortGroupBeans = (List<RsBWMAppPortGroupEntry>) sourceDevice.readAll(srcAppPortGroupEntry)
        RsBWMAppPortGroupEntry dstAppPortGroupEntry = new RsBWMAppPortGroupEntry()


        destinationDevices.each { dp ->
            //log.info("started app port sync + black white lists")
            log.info String.format("working on dp %s", dp.getManagementIp())
                    /*
             sync App Port Groups bean
             * */

            List<RsBWMAppPortGroupEntry> beansToDeleteAppPortGroup = []
            List<RsBWMAppPortGroupEntry> matchedDstAppPortGroupBeans = []
            List<RsBWMAppPortGroupEntry> dstAppPortGroupBeans = (List<RsBWMAppPortGroupEntry>) dp.readAll(dstAppPortGroupEntry)

            if (!srcAppPortGroupBeans.isEmpty()) {
                log.info ("started application port group sync")
                if (dstAppPortGroupBeans.isEmpty()) {
                    log.info String.format("destination DP %s App port Group is empty", dp.getManagementIp())
                    for (RsBWMAppPortGroupEntry srcBean : srcAppPortGroupBeans) {
                        if (srcBean.getType().toString() != "STATIC" && !deleteUnusedPolicies) {
                            dp.create(srcBean)
                        }
                    }
                } else {
                    for (RsBWMAppPortGroupEntry srcBean : srcAppPortGroupBeans) {
                        foundMatchingBean = false
                        for (RsBWMAppPortGroupEntry dstBean : dstAppPortGroupBeans) {
                            String diffed = diffFunctions.diffAppPortGroups(srcBean, dstBean)
                            if (diffed == "matched") {
                                log.debug String.format("bean src: %s matched dst: %s", srcBean, dstBean)
                                foundMatchingBean = true
                                matchedDstAppPortGroupBeans.add(dstBean)
                                break
                            } else if (diffed == "update" && srcBean.getType().toString() != "STATIC") {
                                foundMatchingBean = true
                                matchedDstAppPortGroupBeans.add(dstBean)
                                log.debug String.format("bean src: %s needs to be updated dst: %s", srcBean, dstBean)
                                if (!deleteUnusedPolicies){
                                    log.info String.format("bean src: %s needs to be updated dst: %s, " +
                                            "but delete spare policies is checked", srcBean, dstBean)
                                    dp.update(srcBean)
                                }
                                break
                            }
                        }
                        if (!foundMatchingBean && srcBean.getType().toString() != "STATIC" && !deleteUnusedPolicies) {
                            log.debug String.format("dest device has no bean %s", srcBean.getName())
                            dp.create(srcBean)
                        }
                    }
                }
                /*if (matchedDstAppPortGroupBeans && deleteUnusedPolicies){
                    log.info("delete of app port group beans started")
                    deleteSparePolicies(matchedDstAppPortGroupBeans, dstAppPortGroupBeans, beansToDeleteAppPortGroup, dp)
                }*/
            }

            if (matchedDstAppPortGroupBeans && deleteUnusedPolicies){
                log.info("delete of app port group beans started")
                deleteSparePolicies(matchedDstAppPortGroupBeans, dstAppPortGroupBeans, beansToDeleteAppPortGroup, dp)
            }

            dp.commit()
        }






    }

    public static syncBlockAllow(DeviceConnection sourceDevice, List<DeviceConnection> destinationDevices,
                              Boolean deleteUnusedPolicies){
        /*
        test of sync white list bean
        Read All
        * */
        DiffFunctions diffFunctions = new DiffFunctions()
        boolean foundMatchingBean

        RsNewWhiteListEntry srcWhiteListEntry = new RsNewWhiteListEntry()
        List<RsNewWhiteListEntry> srcWLBeans = (List<RsNewWhiteListEntry>) sourceDevice.readAll(srcWhiteListEntry)
        RsNewWhiteListEntry dstWhiteListEntry = new RsNewWhiteListEntry()

        RsNewBlackListEntry srcBlackListEntry = new RsNewBlackListEntry()
        List<RsNewBlackListEntry> srcBLBeans = (List<RsNewBlackListEntry>) sourceDevice.readAll(srcBlackListEntry)
        RsNewBlackListEntry dstBlackListEntry = new RsNewBlackListEntry()

/*        RsBWMAppPortGroupEntry srcAppPortGroupEntry = new RsBWMAppPortGroupEntry()
        List<RsBWMAppPortGroupEntry> srcAppPortGroupBeans = (List<RsBWMAppPortGroupEntry>) sourceDevice.readAll(srcAppPortGroupEntry)
        RsBWMAppPortGroupEntry dstAppPortGroupEntry = new RsBWMAppPortGroupEntry()*/

        RsBWMPhysicalPortGroupEntry srcPhysicalPortGroupEntry = new RsBWMPhysicalPortGroupEntry()
        List<RsBWMPhysicalPortGroupEntry> srcPhysicalPortGroupBeans = (List<RsBWMPhysicalPortGroupEntry>) sourceDevice.readAll(srcPhysicalPortGroupEntry)
        RsBWMPhysicalPortGroupEntry dstPhysicalPortGroupEntry = new RsBWMPhysicalPortGroupEntry()


                /*
        sync App Port Groups bean
        * */
        syncAppPortGroup(sourceDevice, destinationDevices, deleteUnusedPolicies)

        destinationDevices.each { dp ->
            //log.info("started app port sync + black white lists")
            log.info String.format("working on dp %s", dp.getManagementIp())

/*

            */
/*
        sync App Port Groups bean
        * *//*


            List<RsBWMAppPortGroupEntry> beansToDeleteAppPortGroup = []
            List<RsBWMAppPortGroupEntry> matchedDstAppPortGroupBeans = []
            List<RsBWMAppPortGroupEntry> dstAppPortGroupBeans = (List<RsBWMAppPortGroupEntry>) dp.readAll(dstAppPortGroupEntry)

            if (!srcAppPortGroupBeans.isEmpty()) {
                log.info ("started application port group sync")
                if (dstAppPortGroupBeans.isEmpty()) {
                    log.info String.format("destination DP %s App port Group is empty", dp.getManagementIp())
                    for (RsBWMAppPortGroupEntry srcBean : srcAppPortGroupBeans) {
                        if (srcBean.getType().toString() != "STATIC" && !deleteUnusedPolicies) {
                            dp.create(srcBean)
                        }
                    }
                } else {
                    for (RsBWMAppPortGroupEntry srcBean : srcAppPortGroupBeans) {
                        foundMatchingBean = false
                        for (RsBWMAppPortGroupEntry dstBean : dstAppPortGroupBeans) {
                            String diffed = diffFunctions.diffAppPortGroups(srcBean, dstBean)
                            if (diffed == "matched") {
                                log.debug String.format("bean src: %s matched dst: %s", srcBean, dstBean)
                                foundMatchingBean = true
                                matchedDstAppPortGroupBeans.add(dstBean)
                                break
                            } else if (diffed == "update" && srcBean.getType().toString() != "STATIC") {
                                foundMatchingBean = true
                                matchedDstAppPortGroupBeans.add(dstBean)
                                log.debug String.format("bean src: %s needs to be updated dst: %s", srcBean, dstBean)
                                if (!deleteUnusedPolicies){
                                    log.info String.format("bean src: %s needs to be updated dst: %s, " +
                                            "but delete spare policies is checked", srcBean, dstBean)
                                    dp.update(srcBean)
                                }
                                break
                            }
                        }
                        if (!foundMatchingBean && srcBean.getType().toString() != "STATIC" && !deleteUnusedPolicies) {
                            log.debug String.format("dest device has no bean %s", srcBean.getName())
                            dp.create(srcBean)
                        }
                    }
                }
                */
/*if (matchedDstAppPortGroupBeans && deleteUnusedPolicies){
                    log.info("delete of app port group beans started")
                    deleteSparePolicies(matchedDstAppPortGroupBeans, dstAppPortGroupBeans, beansToDeleteAppPortGroup, dp)
                }*//*

            }
*/




            /*
        sync Physical Port Group bean
        * */

            List<RsBWMPhysicalPortGroupEntry> beansToDeletePhysicalPortGroup = []
            List<RsBWMPhysicalPortGroupEntry> matchedDstPhysicalPortGroupBeans = []
            List<RsBWMPhysicalPortGroupEntry> dstPhysicalPortGroupBeans = (List<RsBWMPhysicalPortGroupEntry>) dp.readAll(dstPhysicalPortGroupEntry)

            if (!srcPhysicalPortGroupBeans.isEmpty()) {
                log.info ("started physical port group sync")

                if (dstPhysicalPortGroupBeans.isEmpty()) {
                    log.info String.format("destination DP %s physical port Group is empty", dp.getManagementIp())
                    for (RsBWMPhysicalPortGroupEntry srcBean : srcPhysicalPortGroupBeans) {
                        //if (srcBean.getType().toString() != "STATIC") {
                        dp.create(srcBean)
                        //}
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
                                if (!deleteUnusedPolicies){
                                    log.debug String.format("bean src: %s updated dst: %s", srcBean, dstBean)
                                    dp.update(srcBean)
                                }else{
                                    log.info String.format("bean src: %s needs to be updated dst: %s, " +
                                            "but delete spare policies is checked", srcBean, dstBean)
                                }
                                break
                            }
                        }
                        if (!foundMatchingBean && !deleteUnusedPolicies) {
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



            /*
        sync white list bean
        * */
            List<RsNewWhiteListEntry> beansToDeleteWL = []
            List<RsNewWhiteListEntry> matchedDstWLBeans = []
            List<RsNewWhiteListEntry> dstWLBeans = (List<RsNewWhiteListEntry>) dp.readAll(dstWhiteListEntry)

            if (!srcWLBeans.isEmpty()) {
                log.info ("started white list sync")
                if (dstWLBeans.isEmpty()) {
                    log.info String.format("destination DP %s whitelist is empty", dp.getManagementIp())
                    for (RsNewWhiteListEntry srcBean : srcWLBeans) {
                        dp.create(srcBean)
                    }
                } else {
                    for (RsNewWhiteListEntry srcBean : srcWLBeans) {
                        foundMatchingBean = false
                        for (RsNewWhiteListEntry dstBean : dstWLBeans) {
                            String diffed = diffFunctions.diffWhiteList(srcBean, dstBean)
                            if (diffed == "matched") {
                                log.debug String.format("bean src: %s matched dst: %s", srcBean, dstBean)
                                foundMatchingBean = true
                                matchedDstWLBeans.add(dstBean)
                                break
                            } else if (diffed == "update") {
                                foundMatchingBean = true
                                matchedDstWLBeans.add(dstBean)
                                log.debug String.format("bean src: %s needs to be updated dst: %s", srcBean, dstBean)
                                if (!deleteUnusedPolicies){
                                    log.info String.format("bean src: %s needs to be updated dst: %s, " +
                                            "but delete spare policies is checked", srcBean, dstBean)
                                    dp.update(srcBean)
                                }
                                break
                            }
                        }
                        if (!foundMatchingBean && !deleteUnusedPolicies) {
                            log.debug String.format("dest device has no bean %s", srcBean.getName())
                            RsNewWhiteListEntry newDstBean = new RsNewWhiteListEntry(
                                    name: srcBean.getName(),
                                    description: srcBean.getDescription(),
                                    direction: srcBean.getDirection(),
                                    dstNetwork: srcBean.getDstNetwork(),
                                    dstPortGroup: srcBean.getDstPortGroup(),
                                    physicalPort: srcBean.getPhysicalPort(),
                                    protocol: srcBean.getProtocol(),
                                    srcNetwork: srcBean.getSrcNetwork(),
                                    srcPortGroup: srcBean.getSrcPortGroup(),
                                    state: srcBean.getState())
                            dp.create(newDstBean)
                        }
                    }

                }
//                if (matchedDstWLBeans && deleteUnusedPolicies){
//                    log.info("delete of white lists bean started")
//                    deleteSparePolicies(matchedDstWLBeans, dstWLBeans, beansToDeleteWL, dp)
//                }
            }

            /*
        sync black list bean
        * */

            List<RsNewBlackListEntry> beansToDeleteBL = []
            List<RsNewBlackListEntry> matchedDstBLBeans = []
            List<RsNewBlackListEntry> dstBLBeans = (List<RsNewBlackListEntry>) dp.readAll(dstBlackListEntry)

            if (!srcBLBeans.isEmpty()) {
                log.info ("started black list sync")
                if (dstBLBeans.isEmpty()) {
                    log.info String.format("destination DP %s blacklist is empty", dp.getManagementIp())
                    for (RsNewBlackListEntry srcBean : srcBLBeans) {
                        RsNewBlackListEntry newDstBean = new RsNewBlackListEntry(
                                name: srcBean.getName(),
                                description: srcBean.getDescription(),
                                direction: srcBean.getDirection(),
                                dstNetwork: srcBean.getDstNetwork(),
                                dstPortGroup: srcBean.getDstPortGroup(),
                                physicalPort: srcBean.getPhysicalPort(),
                                protocol: srcBean.getProtocol(),
                                srcNetwork: srcBean.getSrcNetwork(),
                                srcPortGroup: srcBean.getSrcPortGroup(),
                                state: srcBean.getState())
                        dp.create(newDstBean)
                    }
                } else {
                    for (RsNewBlackListEntry srcBean : srcBLBeans) {
                        foundMatchingBean = false
                        for (RsNewBlackListEntry dstBean : dstBLBeans) {
                            String diffed = diffFunctions.diffBlackList(srcBean, dstBean)
                            if (diffed == "matched") {
                                log.debug String.format("bean src: %s matched dst: %s", srcBean, dstBean)
                                foundMatchingBean = true
                                matchedDstBLBeans.add(dstBean)
                                break
                            } else if (diffed == "update") {
                                foundMatchingBean = true
                                matchedDstBLBeans.add(dstBean)
                                log.debug String.format("bean src: %s needs to be updated dst: %s", srcBean, dstBean)
                                if (!deleteUnusedPolicies){
                                    log.info String.format("bean src: %s needs to be updated dst: %s, " +
                                            "but delete spare policies is checked", srcBean, dstBean)
                                    dp.update(srcBean)
                                }
                                break
                            }
                        }
                        if (!foundMatchingBean && !deleteUnusedPolicies) {
                            log.debug String.format("dest device has no bean %s", srcBean.getName())
                            RsNewBlackListEntry newDstBean = new RsNewBlackListEntry(
                                    name: srcBean.getName(),
                                    description: srcBean.getDescription(),
                                    direction: srcBean.getDirection(),
                                    dstNetwork: srcBean.getDstNetwork(),
                                    dstPortGroup: srcBean.getDstPortGroup(),
                                    physicalPort: srcBean.getPhysicalPort(),
                                    protocol: srcBean.getProtocol(),
                                    srcNetwork: srcBean.getSrcNetwork(),
                                    srcPortGroup: srcBean.getSrcPortGroup(),
                                    state: srcBean.getState())
                            dp.create(newDstBean)
                        }
                    }
                }
//                if (matchedDstBLBeans && deleteUnusedPolicies){
//                    log.info("delete of black list bean started")
//                    deleteSparePolicies(matchedDstBLBeans, dstBLBeans, beansToDeleteBL, dp)
//                }
            }
            /*commit for each dp*/
            dp.commit()

            /*
             Delete Spare Beans
             */

            if (matchedDstBLBeans && deleteUnusedPolicies){
                log.info("delete of black list bean started")
                deleteSparePolicies(matchedDstBLBeans, dstBLBeans, beansToDeleteBL, dp)
            }

            if (matchedDstWLBeans && deleteUnusedPolicies){
                log.info("delete of white lists bean started")
                deleteSparePolicies(matchedDstWLBeans, dstWLBeans, beansToDeleteWL, dp)
            }

/*            if (matchedDstAppPortGroupBeans && deleteUnusedPolicies){
                log.info("delete of app port group beans started")
                deleteSparePolicies(matchedDstAppPortGroupBeans, dstAppPortGroupBeans, beansToDeleteAppPortGroup, dp)
            }*/

            if (matchedDstPhysicalPortGroupBeans && deleteUnusedPolicies){
                log.info("delete of Physical Port Group bean started")
                deleteSparePolicies(matchedDstPhysicalPortGroupBeans, dstPhysicalPortGroupBeans, beansToDeletePhysicalPortGroup, dp)
            }

            dp.commit()
        }
    }

    static def deleteSparePolicies(matchedDestDeviceBeans, dstBeans, beansToDelete, dp){
            dstBeans.each{ dstBean ->
                if(!matchedDestDeviceBeans.contains(dstBean)){
                    log.info String.format("adding bean %s to delete - ", dstBean)
                    beansToDelete.add(dstBean)
                }
            }
        if(!beansToDelete.isEmpty()){
            log.debug String.format("beans to delete list %s", beansToDelete.toListString())
            log.info String.format("going to delete all spare bean on dp - %s", dp.getManagementIp())
            beansToDelete.each {bean ->
                dp.delete(bean)
            }
            dp.commit()
        }
    }

}

