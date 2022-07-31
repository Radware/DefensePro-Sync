package com.radware.vdirect.defensepro

import com.radware.alteon.workflow.impl.java.UpgradeWorkflow
import com.radware.alteon.workflow.impl.WorkflowState
import com.radware.vdirect.scripting.PluginVersion

import com.radware.alteon.api.AdcCLIConnection
import com.radware.alteon.api.AdcConnection
import com.radware.alteon.api.AdcRuntimeException
import com.radware.alteon.api.AdcTemplateResult
import com.radware.alteon.workflow.AdcWorkflowException
import com.radware.alteon.workflow.impl.WorkflowState
import com.radware.alteon.workflow.impl.java.ConfigurationTemplate
import com.radware.alteon.workflow.impl.java.UpgradeWorkflow
import com.radware.defensepro.v8.beans.*
import com.radware.alteon.workflow.impl.DeviceConnection
import com.radware.alteon.workflow.impl.WorkflowAdaptor
import com.radware.alteon.workflow.impl.java.Device
import com.radware.alteon.workflow.impl.java.Param
import com.radware.alteon.workflow.impl.java.Workflow
import com.radware.alteon.workflow.impl.java.Action
import com.radware.alteon.workflow.impl.java.Outputs
import com.radware.vdirect.Password
import com.radware.vdirect.client.api.DeviceType
import com.radware.vdirect.client.api.IDefenseProBeanFactory
import com.radware.vdirect.scripting.PluginVersion
import com.radware.vdirect.scripting.RunAs
import com.radware.vdirect.server.VDirectServerClient
import com.sun.org.apache.xpath.internal.operations.Bool
import groovy.text.GStringTemplateEngine
import org.springframework.beans.factory.annotation.Autowired

import org.apache.http.entity.mime.HttpMultipartMode

import org.apache.http.entity.mime.MultipartEntityBuilder

import org.slf4j.Logger
import org.slf4j.LoggerFactory

@SuppressWarnings(["unused", "GrMethodMayBeStatic"])
@Workflow(
        //createAction='run',
        properties = [
        ]
)
class CopyConfig {
    public static final String exportPath = "/dynamic/Security/Certificates/Export"
    public static final String importPath = "/dynamic/Security/Certificates/Import"
    @Autowired
    WorkflowAdaptor workflow
    @Autowired
    VDirectServerClient vdirect
    @Autowired
    IDefenseProBeanFactory factory

    public static final Logger log = LoggerFactory.getLogger(CopyConfig.class)

    private checkForInterrupt() {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("interrupted")
        }
    }

    static String addressToString(byte[] address) {
        InetAddress addr = InetAddress.getByAddress(address)
        return addr.getHostAddress()
    }

    @UpgradeWorkflow
    static WorkflowState upgrade (VDirectServerClient client, PluginVersion version, WorkflowState state) {

        println "Doing ugrade from version ${version}"
        println "State = ${state.state}"
        println "Props = ${state.parameters}"

        state
    }

    @Action(runAs = RunAs.immediate,
            resultType = 'text/plain',
            consentRequired = false,
            visible = false)
    @Outputs(@Param(name = 'output', type = 'string'))
    String listBeanAttributesForAll(
            @Device(name = 'srcDP', type = DeviceType.defensePro) DeviceConnection srcDP) {
        StringBuffer sb = new StringBuffer()
        RsBWMNetworkEntry srcBwmNetworkEntry = new RsBWMNetworkEntry()
        List<RsBWMNetworkEntry> srcBeans = (List<RsBWMNetworkEntry>) srcDP.readAll(srcBwmNetworkEntry)
        Integer counter = 0
        for (RsBWMNetworkEntry bean : srcBeans) {
            counter++
            addToString(sb, "counter: ", counter.toString())
            Map<String, Object> properties = bean.getProperties()
            for (String key : properties.keySet()) {
                Object value = properties.get(key)
                if (key in ["fromIP", "toIP", "address"]) {
                    value = addressToString(value as byte[])
                }
                addToString(sb, key + ": ", value.toString())
            }
            sb.append("----------\n")
        }
        workflow['output'] = sb.toString()
        return sb.toString()

    }

    public addToString(StringBuffer sb, String title, String value) {
        sb.append(title)
        sb.append(value)
        sb.append("\n")
    }

    @Action(runAs = RunAs.immediate,
            resultType = 'text/plain',
            consentRequired = false,
            visible = false)
    @Outputs(@Param(name = 'output', type = 'string'))
    String listOneBeanWithMethods(
            @Device(name = 'srcDP', type = DeviceType.defensePro) DeviceConnection srcDP) {
        StringBuffer sb = new StringBuffer()
        RsBWMNetworkEntry bwmNetworkEntry = new RsBWMNetworkEntry()
        RsBWMNetworkEntry entry2 = (RsBWMNetworkEntry) srcDP.read(bwmNetworkEntry)
        addToString(sb, "name: ", entry2.getName())
        addToString(sb, "address: ", addressToString(entry2.getAddress()))
        addToString(sb, "fromIP: ", addressToString(entry2.getFromIP()))
        addToString(sb, "mask: ", entry2.getMask())
        addToString(sb, "mode: ", entry2.getMode().toString())
        addToString(sb, "status: ", entry2.getStatus().toString())
        addToString(sb, "subIndex: ", entry2.getSubIndex().toString())
        addToString(sb, "toIp: ", addressToString(entry2.getToIP()))
        workflow['output'] = sb.toString()
        return sb.toString()
    }

    @Action(runAs = RunAs.immediate,
            resultType = 'text/plain',
            consentRequired = false,
            visible = false)
    @Outputs(@Param(name = 'output', type = 'string'))
    String listOneBeanWithProperties(
            @Device(name = 'srcDP', type = DeviceType.defensePro) DeviceConnection srcDP,
            @Param(name = 'beanString', type = 'string', prompt = 'bean string', required = false) String beanString) {
        StringBuffer sb = new StringBuffer()
        RsNewWhiteListEntry entry = new RsNewWhiteListEntry()
        RsNewWhiteListEntry bean = (RsNewWhiteListEntry) srcDP.read(entry)

        Map<String, Object> properties = bean.getProperties()
        for (String key : properties.keySet()) {
            Object value = properties.get(key)
            if ((key in ["fromIP", "toIP", "address"]) && (beanString == "RsBWMNetworkEntry")) {
                value = addressToString(value as byte[])
            }
            addToString(sb, key + ": ", value.toString())
        }
        workflow['output'] = sb.toString()
        return sb.toString()
    }

    String diff(RsBWMNetworkEntry src, RsBWMNetworkEntry dst) {
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


    @Action(visible = true)
    void testEmail(
            @Param(name = "SMTPIP", prompt = 'SMTP IP', defaultValue = "192.168.1.1", type = 'ip') String smtpHost,
            @Param(name = "UserName", type = 'string', defaultValue = "user") String mailUserName,
            @Param(name = "UserPassword", type = 'string', format = 'password', required = false) String mailUserPassword,
            @Param(name = "TLS", type = 'string', defaultValue = "true", values = ["true", "false"], required = true) String TLS,
            @Param(name = "From", type = 'string', defaultValue = "from@org.com") String from,
            @Param(name = "To", type = 'string', defaultValue = "to@org.com") String recipient
    ) {
        try {
            log.info('Testing Email')
            SendMail.sendMail(recipient, from, mailUserName, mailUserPassword, smtpHost, "subjtest", "testbody", TLS)
        } catch (Exception e) {
            log.error String.format("send mail failed %s", e)
        }
    }

    @Action(runAs = RunAs.immediate,
            resultType = 'text/html',
            consentRequired = false,
            visible = true)
    @Outputs([
            @Param(name = 'networkExportErrors', type = 'string[]'),
            @Param(name = 'networkImportErrors', type = 'Object[]'),
            @Param(name = 'networkNameImportErrors', type = 'string[]'),
            @Param(name = 'serverExportErrors', type = 'string[]'),
            @Param(name = 'serverImportErrors', type = 'Object[]')
    ])
    String SyncAction(
            @Device(name = 'srcDP', type = DeviceType.defensePro, prompt = "Source DP", maxLength = 1) DeviceConnection srcDP,
            @Device(name = 'dstDP', type = DeviceType.defensePro, prompt = "Dest DP", maxLength = -1) DeviceConnection[] dstDP,
            @Param(name = 'chooseMasterByBw', type = 'bool', prompt = 'Selecting Source Device Dynamically (Current Higher BW)',
                    defaultValue = 'false') boolean chooseMasterByBw,
            @Param(name = 'safeRevert', type = 'bool', prompt = 'Revert Config Upon Import \n Policy Failure',
                    defaultValue = 'true') boolean safeRevert,
            @Param(name = 'deleteUnusedPolicies', type = 'bool', prompt = 'Delete Passive Device Spare Policies',
                    defaultValue = 'false') boolean deleteUnusedPolicies,
            @Param(name = 'syncSslCertificates', type = 'bool', prompt = 'Sync SSL Certificates',
                    defaultValue = 'false') boolean syncSslCertificates,
            @Param(name = 'userName', prompt = "Devices User Name", type = 'string', defaultValue = "radware",
                    uiVisible = "syncSslCertificates",uiRequired = "syncSslCertificates", required = false) String userName,
            @Param(name = 'userPassword', prompt = "Devices User Password", type = 'string', format = 'password',
                    uiVisible = "syncSslCertificates",uiRequired = "syncSslCertificates", defaultValue = "radware?",
                    required = false) String userPassword,
            @Param(name = 'syncConfiguration', type = 'bool', prompt = 'Sync Configuration',
                    defaultValue = 'true') boolean syncConfiguration,
            @Param(name = 'syncBlackWhiteList', type = 'bool',
                    prompt = 'Sync Block\\Allow lists \n (includes NetworkClasses)', uiEditable = "true",
                    defaultValue = 'false') boolean syncBlackWhiteList,
            @Param(name = 'nwDnsBaseline', type = 'bool', prompt = 'DNS Baselines',
                    defaultValue = 'true') boolean nwDnsBaseline,
            @Param(name = 'nwBdosBaseline', type = 'bool', prompt = 'BDoS Baselines',
                    defaultValue = 'true') boolean nwBdosBaseline,
            @Param(name = 'networkPoliciesExecptions', type = 'string[]',
                    prompt = 'Network Protection -BASELINES- \n Exception List (Case Sensitive)',
                    required = true, defaultValue = '[]') String[] networkPoliciesExecptions,
            @Param(name = 'srvHttpBaseline', type = 'bool', prompt = 'HTTP Baselines (DP Ver. 6 & 7)',
                    defaultValue = 'false', uiEditable = "false") boolean srvHttpBaseline,
            @Param(name = 'sendSMTPReport', type = 'bool', prompt = 'Mail Notification',
                    defaultValue = 'false') boolean sendSMTPReport,
            @Param(name = "SMTPIP", defaultValue = "192.168.1.1", prompt = 'SMTP IP',
                    type = 'ip', uiVisible = "sendSMTPReport", uiRequired = "sendSMTPReport",
                    required=false) String smtpHost,
            @Param(name = "TLS", type = 'string', defaultValue = "true", values = ["true", "false"],
                    uiVisible = "sendSMTPReport",uiRequired = "sendSMTPReport", required = false) String TLS,
            @Param(name = "MailUserName", type = 'string', defaultValue = "user", uiVisible = "sendSMTPReport",
                    uiRequired = "sendSMTPReport", required=false) String mailUserName,
            @Param(name = "MailUserPassword", type = 'string', format = 'password',
                    uiVisible = "sendSMTPReport", required=false) String mailUserPassword,
            @Param(name = "From", type = 'string', defaultValue = "from@org.com",
                    uiVisible = "sendSMTPReport",uiRequired = "sendSMTPReport", required=false) String from,
            @Param(name = "To", type = 'string', defaultValue = "to@org.com",
                    uiVisible = "sendSMTPReport",uiRequired = "sendSMTPReport", required=false) String recipient) {
        String testOutputResult = "AA"
        try {
            def version = vdirect.getWorkflowManager().getWorkflowTemplate("dpSync").version
            log.info("Workflow version: ${version}")

            log.info("Started Sync")

            List<String> sslCertsExportErrors = []
            List<String> sslCertsImportErrors = []
            Integer sslCertsExportSum = 0
            Integer sslCertsImportSum = 0



            List<DeviceConnection> deviceConnectionList = new ArrayList<>()
            deviceConnectionList.add(srcDP)
            dstDP.each { dp ->
                if (dp != srcDP) {
                    deviceConnectionList.add(dp)
                }
            }
            DeviceConnection masterDevice
            if (!chooseMasterByBw) {
                log.info("syslog-prefix Selection of Active device is set staticly")
                deviceConnectionList.remove(srcDP)
                masterDevice = srcDP
                log.info("Source Node: ${srcDP.getManagementIp()}")
                deviceConnectionList.each { dp ->
                    log.info String.format("passove devices: ${dp.getManagementIp()}")
                }
            } else {
                int masterDeviceBandwidth
                log.info("syslog-prefix Active node detection started.")
                deviceConnectionList.each { dp ->
                    RsGeneric entry = new RsGeneric()
                    int bw = 0
                    for (i in [1..3]) {
                        RsGeneric bean = (RsGeneric) dp.read(entry)
                        bw = bw + bean.rsPortStatsTotalInMbitsPerSec
                    }
                    if (masterDevice == null) {
                        masterDevice = dp
                        masterDeviceBandwidth = bw
                    } else if (bw > masterDeviceBandwidth) {
                        masterDeviceBandwidth = bw
                        masterDevice = dp
                    }
                }
                masterDeviceBandwidth = masterDeviceBandwidth.intdiv(3)
                deviceConnectionList.remove(masterDevice)
                log.info("syslog-prefix Active Node is - ${masterDevice.getManagementIp()} based on ${masterDeviceBandwidth} mbps.")
                deviceConnectionList.each { dp ->
                    log.info String.format("syslog-prefix Passive devices %s", dp.getManagementIp())
                }
            }

            //check version competability
            Integer majorVer = CheckVersions.compare(masterDevice, deviceConnectionList)
            log.info String.format("major ver is %s", majorVer)

            if (syncSslCertificates){
                log.info("started certificate sync")
                deviceConnectionList.each {dp ->
                    log.info String.format("working on dp %s", dp.getManagementIp())
                    RsSSLCertificateEntry srcCertEntries = new RsSSLCertificateEntry()
                    List<RsSSLCertificateEntry> srcCertBeans = (List<RsSSLCertificateEntry>) masterDevice.readAll(srcCertEntries)
                    RsSSLCertificateEntry dstCertEntries = new RsSSLCertificateEntry()
                    List<RsSSLCertificateEntry> dstCertBeans = (List<RsSSLCertificateEntry>) dp.readAll(dstCertEntries)

                    def dstCertsNameArray = []
                    dstCertBeans.each { idx ->
                        dstCertsNameArray.add(idx.name)
                    }
                    srcCertBeans.removeAll { it.name in dstCertsNameArray }

                    String Auth = userName + ":" + userPassword
                    String EncodedAuth = Base64.getEncoder().encodeToString(Auth.getBytes());
                    String authHeader = "Basic " + new String(EncodedAuth);

                    def httpExport = CertExportImport.getHttp(masterDevice.getManagementIp(),
                            exportPath, authHeader)
                    def httpImport = CertExportImport.getHttp(dp.getManagementIp(),
                            importPath, authHeader)

                    if (srcCertBeans.size() > 0) {
                        log.info String.format("There is a gap in certificates, syncing certs, devices: %s, %s",
                                masterDevice.getManagementIp(), dp.getManagementIp())

                        srcCertBeans.each { idx ->
                            try {
                                def exportKeyResult
                                def exportCertResult
                                def importKeyResult
                                def importCertResult

                                exportKeyResult = CertExportImport.exportCert(httpExport, idx.name, "KEY",
                                        authHeader, idx.keyPassphrase)
                                exportCertResult = CertExportImport.exportCert(httpExport, idx.name,
                                        "Certificate", authHeader, idx.keyPassphrase)

                                log.debug String.format("Export key result %s , Export cert result %s", exportKeyResult.success,
                                        exportCertResult.get('success'))

                                if (exportKeyResult.get('success') && exportCertResult.get('success')) {
                                    sslCertsExportSum += 1
                                    importKeyResult = CertExportImport.importCert(httpImport, idx.name, "KEY",
                                            authHeader, idx.keyPassphrase, exportKeyResult.get('pkiObj').toString())
                                    importCertResult = CertExportImport.importCert(httpImport, idx.name,
                                            "Certificate", authHeader, idx.keyPassphrase,
                                            exportCertResult.get('pkiObj').toString())

                                    if (importKeyResult.get('success') && importCertResult.get('success')) {
                                        sslCertsImportSum += 1
                                        log.info String.format("Successfully imported cert %s", idx.name)
                                    } else {
                                        sslCertsImportErrors.add String.format("Name: %s", idx.name)
                                        log.info String.format("import key result %s ,import cert result %s",
                                                importKeyResult.success, importCertResult.success)
                                        log.error String.format("import key response %s , import cert response %s",
                                                importKeyResult.response, importCertResult.response)
                                        // TODO
                                        // add error to mail
                                    }
                                }else{
                                    sslCertsExportErrors.add String.format("Name: %s", idx.name)
                                }
                            }
                            catch (Exception e) {
                                log.error String.format('failed in cert/key action %s', e)
                                // TODO
                                // add error to output mail
                            }
                        }
                    } else {
                        log.info String.format("Certs on devices %s, %s are identical", srcDP.getManagementIp(), dp.getManagementIp())
                    }
                }
            }

            if (syncBlackWhiteList || syncConfiguration){
                Helpers.syncPhysicalPortGroup(masterDevice, deviceConnectionList)
            }
            if (syncBlackWhiteList) {
                SyncNetworkClass.syncNwClass(masterDevice, deviceConnectionList, deleteUnusedPolicies)
                SyncBlockAllow.syncBlockAllow(masterDevice, deviceConnectionList, deleteUnusedPolicies)
            }

            if (syncBlackWhiteList) {
                SyncNetworkClass.syncNwClass(masterDevice, deviceConnectionList, false)
                SyncBlockAllow.syncBlockAllow(masterDevice, deviceConnectionList, false)
            }
            if (syncBlackWhiteList) {
                SyncBlockAllow.syncBlockAllow(masterDevice, deviceConnectionList, deleteUnusedPolicies)
                SyncNetworkClass.syncNwClass(masterDevice, deviceConnectionList, deleteUnusedPolicies)
            }

            def tempResult = runTemplate('dpSync.vm', ['masterDevice' : masterDevice, 'standByDevices': deviceConnectionList, 'deleteUnusedPolicies': deleteUnusedPolicies,
                                                       'syncConfiguration': syncConfiguration, 'nwDnsBaseline': nwDnsBaseline,
                                                       'nwBdosBaseline'   : nwBdosBaseline, 'networkPoliciesExecptions': networkPoliciesExecptions,
                                                       'srvHttpBaseline'  : srvHttpBaseline, 'safeRevert' : safeRevert])

            try {
                log.info('restart vdirect connection..')
                runTemplate('no-op.vm', ['masterDevice'     : masterDevice, 'standByDevices': deviceConnectionList])
            } catch (Exception restart) {
                //reboot script throws exception to cause vDirect to remove current connection
            }

            if(syncConfiguration){
                SyncPolicyPortGroup.syncPolicyPortGroups(masterDevice, deviceConnectionList, networkPoliciesExecptions)
                deviceConnectionList.each { dp ->
                    dp.commit()
                }
            }
            //return "Output written to vDirect log file"


            String[] networkExportErrors = tempResult['networkExportErrors']
            Object[] networkImportErrors = tempResult['networkImportErrors']
            String[] serverExportErrors = tempResult['serverExportErrors']
            Object[] serverImportErrors = tempResult['serverImportErrors']
            String[] networkNameImportErrors = tempResult['networkNameImportErrors']

            workflow['networkExportErrors'] = networkExportErrors
            workflow['networkImportErrors'] = networkImportErrors
            workflow['networkNameImportErrors'] = networkNameImportErrors
            workflow['serverExportErrors'] = serverExportErrors
            workflow['serverImportErrors'] = serverImportErrors

            def binding = [networkExportErrors: networkExportErrors, networkImportErrors: networkImportErrors, serverExportErrors: serverExportErrors, serverImportErrors: serverImportErrors,
                           networkPoliciesSize: tempResult['networkPoliciesSize'], networkPoliciesErrorSize: tempResult['networkPoliciesErrorSize'],
                           serverPoliciesSize : tempResult['serverPoliciesSize'], serverPoliciesErrorSize: tempResult['serverPoliciesErrorSize'],
                           masterDevice : masterDevice, deviceConnectionList: deviceConnectionList, sslCertsExportErrors: sslCertsExportErrors,
                           sslCertsImportErrors:sslCertsImportErrors, sslCertsExportSum:sslCertsExportSum, sslCertsImportSum:sslCertsImportSum
                           ]

            def template =
                    '''
            <div>

            <center><h2>DP Sync Summary</h2></center>
                 
            <h2>Devices</h2>

            <span>Source: ${masterDevice}</span>
            
            <p>
                <h2>Destinations:</h2>
            <ul>
            <% for (device in deviceConnectionList) { %>
                <li> ${device}
                <% } %>
            </ul>
            </p> 
          
            <style>
            table, th, td {
                border: 1px solid black;
            }
            </style>
            
            <table>
            <caption style="font-weight: bolder;  text-align: left; margin-bottom: 5px; font-size: 160%;    padding: 5px;    letter-spacing: 5px;">Summary </caption>
            <tr>
            <th><font color='blue'>Policy Type</th>
            <th><font color='blue'>Policies</th>
            <th><font color='blue'>Export Errors</th>
            <th><font color='blue'>Import Errors</th>
            </tr>
            
            <tr>
            <td>Network Policies, BDos, DNS, HTTPs Flood Baselines</td>
            <td>${networkPoliciesSize}</td>
            <td>${networkPoliciesErrorSize}</td>
            <td>${networkImportErrors.size()}</td>
            </tr>
            
            <tr>
            <td>Server Policies</td>
            <td>${serverPoliciesSize}</td>
            <td>${serverPoliciesErrorSize}</td>
            <td>${serverImportErrors.size()}</td>
            </tr>
            
            <tr>
            <td>SSL Certificates</td>
            <td>${sslCertsExportSum}</td>
            <td>${sslCertsExportErrors.size()}</td>
            <td>${sslCertsImportErrors.size()}</td>
            </tr>
            
            <tr>
            <td>SSL Keys</td>
            <td>${sslCertsImportSum}</td>
            <td>${sslCertsExportErrors.size()}</td>
            <td>${sslCertsImportErrors.size()}</td>
            </tr>

            
            </table>
            <h2>SSL Cert, Key Export Errors</h2>
            <table border="1">
                <tr>
                 <th>Name</th>
                 </tr>
                 <% for (item in sslCertsExportErrors) { %>
                    <tr>
                    <td>${item}</td>
                    </tr>
                <% } %>
            </table>
            
            </table>
            <h2>SSL Cert, Key Import Errors</h2>
            <table border="1">
                <tr>
                 <th>Policy</th>
                 </tr>
                 <% for (item in sslCertsImportErrors) { %>
                    <tr>
                    <td>${item}</td>
                    </tr>
                <% } %>
            </table>
            
            </table>
            <h2>Network Export Errors</h2>
            <table border="1">
                <tr>
                 <th>Policy</th>
                 <th>Error</th>
                 </tr>
                 <% for (error in networkExportErrors) { %>
                    <tr>
                    <td>${error.policy}</td>
                    <td>${error.error}</td>
                    </tr>
                <% } %>
            </table>

            <h2>Network Import Errors</h2>
            <table border="1">
              <tr>
                  <th>Policy</th>
                  <th>Host</th>
                  <th>Error</th>
               </tr>
               <% for (error in networkImportErrors) { %>
                <tr>
                <td>${error.policy}</td>
                <td>${error.host}</td>
                <td>${error.error}</td>
                </tr>
                <% } %>
            </table>
            
            <h2>Server Export Errors</h2>
            <table border="1">
                <tr>
                  <th>Policy</th>
                  <th>Error</th>
                </tr>
                <%for (error in serverExportErrors) { %>
                    <tr>
                    <td>${error.policy}</td>
                    <td>${error.error}</td>
                    </tr>
                <% } %>
            </table>

            <h2>Server Import Errors</h2>
            <table border="1">
                <tr>
                    <th>Policy</th>
                    <th>Host</th>
                    <th>Error</th>
                </tr>
                <% for (error in serverImportErrors) { %>
                    <tr>
                    <td>${error.policy}</td>
                    <td>${error.host}</td>
                    <td>${error.error}</td>
                    </tr>
                <% } %>
            </table>
            

            </div>
            '''

            def engine = new GStringTemplateEngine()
            //workflow['html'] = engine.createTemplate(template).make(binding).toString()
            //result;
            log.info("End Sync")
            //return engine.createTemplate(template).make(binding).toString()


            testOutputResult = engine.createTemplate(template).make(binding).toString()



            if (sendSMTPReport){
                try {
                    log.info('Send Email')
                    SendMail.sendMail(recipient, from, mailUserName, mailUserPassword, smtpHost, "DP Sync Summary", testOutputResult, TLS)
                } catch (Exception e) {
                    log.error String.format("mail sent failed %s", e)
                }
            }
            return testOutputResult
        } catch (Exception e) {
            log.error String.format("failed to run workflow %s", e.getMessage())
            try {
                log.info("After failure, trying to send error email if SMTP is defined.")
                if (sendSMTPReport) {
                    SendMail.sendMail(recipient, from, mailUserName, mailUserPassword, smtpHost, "Failed Run - DP Sync Summary", e.getMessage(), TLS)
                }
            }catch (Exception b){
                throw new AdcWorkflowException (String.format("failed to run workflow and failed to send mail %s", b.getMessage()))
            }
            throw new AdcWorkflowException (String.format("failed to run workflow %s", e.getMessage()))
        }
    }

    def findFirstFreeBwmCompoundIndex(List<RsBWMNetworkEntry> beans) {
        int firstFreeCompoundIndex = 0
        for (int i = 0; i < beans.size(); i++) {
            if (beans[i].getSubIndex() > firstFreeCompoundIndex) {
                firstFreeCompoundIndex = beans[i].getSubIndex()
                break
            } else if (beans[i].getSubIndex() == beans.size()) {
                firstFreeCompoundIndex = firstFreeCompoundIndex + 1
            } else if (beans[i].getSubIndex() == firstFreeCompoundIndex) {
                firstFreeCompoundIndex = firstFreeCompoundIndex + 1
            }
        }
        return firstFreeCompoundIndex
    }

    def runTemplate(String templateName, Map params) {
        ConfigurationTemplate template = workflow.getTemplate(templateName)
        template.setParameters(params)
        log.debug("Template Name : " + templateName + " PARAMS: " + params.toString())
        AdcTemplateResult templateResult
        try {
            templateResult = template.run()
            log.debug("GENERATED SCRIPT" + templateResult.getGeneratedScript())
            log.debug("CLI OUTPUT" + templateResult.getCliOutput())
            templateResult.parameters
        } catch (AdcRuntimeException re) {
            throw re
        }
        //return templateResult.getResult()
    }
}

