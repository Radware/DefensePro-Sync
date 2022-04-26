package com.radware.vdirect.defensepro

import com.radware.defensepro.v8.beans.RsBWMAppPortGroupEntry
import com.radware.defensepro.v8.beans.RsBWMPhysicalPortGroupEntry
import com.radware.defensepro.v8.beans.RsIDSNewRulesEntry
import com.radware.defensepro.v8.beans.RsNewBlackListEntry
import com.radware.defensepro.v8.beans.RsNewWhiteListEntry
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DiffFunctions {
    CopyConfig copyConfig = new CopyConfig()

    private static final Logger log = LoggerFactory.getLogger(DiffFunctions.class)

    String diffWhiteList(RsNewWhiteListEntry src, RsNewWhiteListEntry dst) {
        if (src.getName().toString() == dst.getName().toString()) {
            StringBuffer srcSb = new StringBuffer()
            StringBuffer dstSb = new StringBuffer()
            Map<String, Object> srcProperties = src.getProperties()
            Map<String, Object> dstProperties = dst.getProperties()
            for (String key : srcProperties.keySet()) {
                Object value = srcProperties.get(key)
                copyConfig.addToString(srcSb, key + ": ", value.toString())
            }
            for (String key : dstProperties.keySet()) {
                Object value = dstProperties.get(key)
                copyConfig.addToString(dstSb, key + ": ", value.toString())
            }
            if (srcSb.toString().equals(dstSb.toString())) {
                log.debug String.format("whitelist match, src: %s : dst: %s", srcSb.toString(), dstSb.toString())
                return "matched"
            } else {
                log.debug String.format("whitelist update, src: %s : dst: %s", srcSb.toString(), dstSb.toString())
                return "update"
            }
        } else {
            log.debug String.format("whitelist notmatched, src: %s : dst: %s", src.getName().toString(), dst.getName().toString())
            return "nonmatched"
        }
    }

    String diffBlackList(RsNewBlackListEntry src, RsNewBlackListEntry dst) {
        if (src.getName().toString() == dst.getName().toString()) {
            StringBuffer srcSb = new StringBuffer()
            StringBuffer dstSb = new StringBuffer()
            Map<String, Object> srcProperties = src.getProperties()
            Map<String, Object> dstProperties = dst.getProperties()
            for (String key : srcProperties.keySet()) {
                Object value = srcProperties.get(key)
                copyConfig.addToString(srcSb, key + ": ", value.toString())
            }
            for (String key : dstProperties.keySet()) {
                Object value = dstProperties.get(key)
                copyConfig.addToString(dstSb, key + ": ", value.toString())
            }
            if (srcSb.toString().equals(dstSb.toString())) {
                log.debug String.format("whitelist match, src: %s : dst: %s", srcSb.toString(), dstSb.toString())
                return "matched"
            } else {
                log.debug String.format("whitelist update, src: %s : dst: %s", srcSb.toString(), dstSb.toString())
                return "update"
            }
        } else {
            log.debug String.format("whitelist notmatched, src: %s : dst: %s", src.getName().toString(), dst.getName().toString())
            return "nonmatched"
        }
    }


    String diffAppPortGroups(RsBWMAppPortGroupEntry src, RsBWMAppPortGroupEntry dst) {
        if (src.getName().toString() == dst.getName().toString()) {
            StringBuffer srcSb = new StringBuffer()
            StringBuffer dstSb = new StringBuffer()
            Map<String, Object> srcProperties = src.getProperties()
            Map<String, Object> dstProperties = dst.getProperties()
            for (String key : srcProperties.keySet()) {
                Object value = srcProperties.get(key)
                copyConfig.addToString(srcSb, key + ": ", value.toString())
            }
            for (String key : dstProperties.keySet()) {
                Object value = dstProperties.get(key)
                copyConfig.addToString(dstSb, key + ": ", value.toString())
            }
            if (srcSb.toString().equals(dstSb.toString())) {
                log.debug String.format("AppPortGroup match, src: %s : dst: %s", srcSb.toString(), dstSb.toString())
                return "matched"
            } else {
                log.debug String.format("AppPortGroup update, src: %s : dst: %s", srcSb.toString(), dstSb.toString())
                return "update"
            }
        } else {
            log.debug String.format("AppPortGroup notmatched, src: %s : dst: %s", src.getName().toString(), dst.getName().toString())
            return "nonmatched"
        }
    }


    String diffPhysicalPortGroups(RsBWMPhysicalPortGroupEntry src, RsBWMPhysicalPortGroupEntry dst) {
        if (src.getName().toString() == dst.getName().toString()) {
            StringBuffer srcSb = new StringBuffer()
            StringBuffer dstSb = new StringBuffer()
            Map<String, Object> srcProperties = src.getProperties()
            Map<String, Object> dstProperties = dst.getProperties()
            for (String key : srcProperties.keySet()) {
                Object value = srcProperties.get(key)
                copyConfig.addToString(srcSb, key + ": ", value.toString())
            }
            for (String key : dstProperties.keySet()) {
                Object value = dstProperties.get(key)
                copyConfig.addToString(dstSb, key + ": ", value.toString())
            }
            if (srcSb.toString().equals(dstSb.toString())) {
                log.debug String.format("AppPortGroup match, src: %s : dst: %s", srcSb.toString(), dstSb.toString())
                return "matched"
            } else {
                log.debug String.format("AppPortGroup update, src: %s : dst: %s", srcSb.toString(), dstSb.toString())
                return "update"
            }
        } else {
            log.debug String.format("AppPortGroup notmatched, src: %s : dst: %s", src.getName().toString(), dst.getName().toString())
            return "nonmatched"
        }
    }

    String diffPolicyPhysicalPortGroups(RsIDSNewRulesEntry src, RsIDSNewRulesEntry dst) {
        if (src.getName().toString() == dst.getName().toString()) {
            StringBuffer srcSb = new StringBuffer()
            StringBuffer dstSb = new StringBuffer()

            String srcPortMask = src.getPortmask()
            String dstPortMask = dst.getPortmask()

            if (srcPortMask == dstPortMask) {
                log.debug String.format("PolicyPortGroup match, src: %s : dst: %s", srcPortMask.toString(), dstPortMask.toString())
                return "matched"
            } else {
                log.debug String.format("PolicyPortGroup update, src: %s : dst: %s", srcPortMask.toString(), dstPortMask.toString())
                return "update"
            }
        } else {
            log.debug String.format("PolicyPortGroup notmatched, src: %s : dst: %s", src.getName().toString(), dst.getName().toString())
            return "nonmatched"
        }
    }
}
