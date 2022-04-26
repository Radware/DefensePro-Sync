package com.radware.vdirect.defensepro

import com.radware.alteon.workflow.impl.DeviceConnection
import com.radware.defensepro.v8.beans.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class SyncNetworkClass {
    public static final Logger log = LoggerFactory.getLogger(SyncNetworkClass.class)

    public static syncNwClass(DeviceConnection sourceDevice, List<DeviceConnection> destinationDevices,
            Boolean deleteUnusedPolicies){


        log.info("started network class sync")
        StringBuffer sb = new StringBuffer()
        RsBWMNetworkEntry srcBwmNetworkEntry = new RsBWMNetworkEntry()
        List<RsBWMNetworkEntry> srcBeans = (List<RsBWMNetworkEntry>) sourceDevice.readAll(srcBwmNetworkEntry)
        if (!srcBeans.isEmpty()) {
            RsBWMNetworkEntry dstBwmNetworkEntry = new RsBWMNetworkEntry()
            destinationDevices.each { dp ->
                List<RsBWMNetworkEntry> beansToDelete = []
                List<RsBWMNetworkEntry> matchedDestDeviceBeans = []

                log.info String.format("working on dp %s", dp.getManagementIp())
                List<RsBWMNetworkEntry> dstBeans = (List<RsBWMNetworkEntry>) dp.readAll(dstBwmNetworkEntry)
                if (dstBeans.isEmpty()) {
                    log.info String.format("destination DP %s nwclass is empty", dp.getManagementIp())
                    for (RsBWMNetworkEntry srcBean : srcBeans) {
                        dp.create(srcBean)
                    }
                } else {
                    Integer counter = 0
                    for (RsBWMNetworkEntry srcBean : srcBeans) {
                        boolean foundMatchingBean = false
                        for (RsBWMNetworkEntry dstBean : dstBeans) {
                            String diffed = diff(srcBean, dstBean)
                            if (diffed.length() > 0) {
                                //addToString(sb, "diff: ", diff(srcBean, dstBean))
                            } else {
                                addToString(sb, "matched: ${srcBean.getName()}", addressToString(srcBean.getAddress()))
                                counter++
                                foundMatchingBean = true
                                matchedDestDeviceBeans.add(dstBean)
                                break
                            }
                            counter++
                            //addToString(sb, "counter: ", counter.toString())
                        }
                        if (!foundMatchingBean) {
                            addToString(sb, "non-matched: ${srcBean.getName()} ", addressToString(srcBean.getAddress()))
                            RsBWMNetworkEntry bwmNetworkEntry = new RsBWMNetworkEntry(name: srcBean.getName(), subIndex: srcBean.getSubIndex())

                            RsBWMNetworkEntry entry2 = (RsBWMNetworkEntry) dp.read(bwmNetworkEntry)

                            if (dp.isEmpty(entry2) && !deleteUnusedPolicies) {
                                log.debug String.format("dest device has no index %s for obj %s with address %s",
                                        srcBean.getSubIndex(), srcBean.getName(), addressToString(srcBean.getAddress()))
                                dp.create(srcBean)
                            } else if (!deleteUnusedPolicies) {
                                log.debug String.format("dest device has index %s for obj %s", srcBean.getSubIndex(), srcBean.getName())
                                log.debug String.format("obj exists %s with ip %s", entry2.getName(), addressToString(entry2.getAddress()))
                                RsBWMNetworkEntry bwmNetworkEntry2 = new RsBWMNetworkEntry(name: srcBean.getName())
                                List<RsBWMNetworkEntry> dstBeans2 = (List<RsBWMNetworkEntry>) dp.readAll(bwmNetworkEntry2)
                                srcBean.setSubIndex(findFirstFreeBwmCompoundIndex(dstBeans2))
                                log.debug String.format("Going to create network class bean name - %s, IP - %s, with index %s",
                                                srcBean.getName(), addressToString(srcBean.getAddress()), srcBean.getSubIndex())
                                dp.create(srcBean)
                            }
                        }
                    }
                }
                dp.commit()
                // Delete Spare Beans
                if (matchedDestDeviceBeans && deleteUnusedPolicies){
                    dstBeans.each{ dstBean ->
                        if(!matchedDestDeviceBeans.contains(dstBean)){
                            log.info String.format("adding bean to delete %s with index %s", dstBean.name,
                                    dstBean.subIndex)
                            beansToDelete.add(dstBean)
                        }
                    }
                    if(beansToDelete){
                        log.debug String.format("beans to delete list %s", beansToDelete.toListString())
                        log.info String.format("going to delete all spare bean on dp - %s", dp.getManagementIp())
                        beansToDelete.each {bean ->
                            dp.delete(bean)
                        }
                        dp.commit()
                    }
                }
            }
        }
    }

    static findFirstFreeBwmCompoundIndex(List<RsBWMNetworkEntry> beans) {
        int firstFreeCompoundIndex = 0
        for (int i = 0; i < beans.size(); i++) {
            if (beans[i].getSubIndex() > firstFreeCompoundIndex) {
                //firstFreeCompoundIndex = beans[i].getSubIndex()
                break
            } else if (beans[i].getSubIndex() == beans.size()) {
                firstFreeCompoundIndex = firstFreeCompoundIndex + 1
            } else if (beans[i].getSubIndex() == firstFreeCompoundIndex) {
                firstFreeCompoundIndex = firstFreeCompoundIndex + 1
            }
        }
        log.debug String.format("Found index %s as free", firstFreeCompoundIndex)
        return firstFreeCompoundIndex
    }

    static String addressToString(byte[] address) {
        InetAddress addr = InetAddress.getByAddress(address)
        return addr.getHostAddress()
    }

    static addToString(StringBuffer sb, String title, String value) {
        sb.append(title)
        sb.append(value)
        sb.append("\n")
    }

    static String diff(src, dst) {
        StringBuffer sb = new StringBuffer()
        def properties = ['name', 'mode', 'fromIP', 'toIP', 'mask', 'address']
        Map<String, Object> srcProps = src.getProperties()
        Map<String, Object> dstProps = dst.getProperties()
        for (String key : properties) {
            String srcValue
            String dstValue
            if (key in ["fromIP", "toIP", "address"]) {
                srcValue = addressToString(srcProps.get(key) as byte[])
                dstValue = addressToString(dstProps.get(key) as byte[])
            } else {
                srcValue = srcProps.get(key) as String
                dstValue = dstProps.get(key) as String
            }
            if (!srcValue.equals(dstValue)) {
                sb.append("| src: ")
                sb.append(srcValue)
                sb.append(", dst: ")
                sb.append(dstValue)
            }
        }
        if (sb.length() > 0) {
            sb.append("\n-----")
        }
        return sb.toString()
    }
}
