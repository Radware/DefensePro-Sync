package com.radware.vdirect.defensepro

import groovyx.net.http.HTTPBuilder
import org.apache.http.HttpEntity
import org.apache.http.auth.AuthenticationException
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.entity.mime.HttpMultipartMode

import groovyx.net.http.HTTPBuilder

import org.apache.http.HttpEntity
import org.apache.http.auth.AuthenticationException
/*import org.apache.http.entity.mime.HttpMultipartMode

import org.apache.http.entity.mime.MultipartEntityBuilder*/
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired

class CertExportImport {

    @Autowired
    Logger log

    static Map exportCert(httpExport, String name,String pkiType, authHeader, String passPhrase)
            throws Exception{
        def result = [:]
        httpExport.request(groovyx.net.http.Method.POST) { req ->
            requestContentType: "multipart/form-data"
            MultipartEntityBuilder builder = MultipartEntityBuilder.create()
                    .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                    .addTextBody("NameCombo", name)
                    .addTextBody("TypeCombo", pkiType)
                    .addTextBody("password1", passPhrase)
                    .addTextBody("certText","")
                    .addTextBody("action","set")
                    .addTextBody("FormatCombo","PEM")
            HttpEntity multiPartEntity = builder.build();
            req.setEntity(multiPartEntity)

            response.success = { resp, reader ->
                result.statusCode = resp.statusLine.statusCode
                if (resp.statusLine.statusCode == 200) {
                    result['pkiObj'] = reader.text
                    result['success'] = true
                }
            }
        }
        return result
    }

    static Map importCert(httpImport, String name,String pkiType, authHeader, String passPhrase, String pkiObject)
            throws Exception{
        def result = [:]
        httpImport.request(groovyx.net.http.Method.POST) { req ->

            requestContentType: "multipart/form-data"

            MultipartEntityBuilder builder = MultipartEntityBuilder.create()
                    .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                    .addTextBody("entName", name)
                    .addTextBody("entryTypeCombo", pkiType)
                    .addTextBody("FormatCombo", "PEM")
                    .addTextBody("password1",passPhrase)
                    .addTextBody("certText", pkiObject)
            HttpEntity multiPartEntity = builder.build();
            req.setEntity(multiPartEntity)

            response.success = { resp, reader ->
                def responseBody = reader.toString()
                result['success'] = resp.status == 200 &&
                        responseBody.contains("Import Finished")
                result['response'] = responseBody
            }
            response.failure = { resp, reader ->
                result['success'] = false
                throw new Exception(String.format("failed to import cert/key %s, due to error", name))
            }
        }
        return result
    }

    static HTTPBuilder getHttp(String host, String path, String authHeader) throws Exception {
        HTTPBuilder http = new HTTPBuilder("https://${host}${path}")
        http.getClient().getParams().setParameter("http.connection.timeout", new Integer(10000))
        http.ignoreSSLIssues()
        http.handler.failure = { resp, data ->
            throw new Exception(String.format('Failed call: URI- %s , Reason- %s', host + path, resp.statusLine))
        }
        http.headers.'Accept-Encoding' = 'application/json'
        http.headers.'Authorization' = authHeader
        http.handler.'401' = { resp -> throw new AuthenticationException("Username or password are invalid") }
        http.autoAcceptHeader

        return http
    }
}
