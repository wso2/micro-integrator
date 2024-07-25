import * as dmUtils from "./dm-utils";

/*
* title : "root",
* inputType : "XML",
*/
interface Root {
    lead: {
        ID: string
        name: string
        city: string
        code: string
        country: string
    }[]
    sendNotificationEmail: string
    convertedStatus: string
    doNotCreateOpportunity: string
    opportunityName: string
    overwriteLeadSource: string
    sessionId: string
}

/*
* title : "soapenv:Envelope",
* outputType : "JSON",
* namespaces :[{"prefix": "soapenv","url": "http://www.w3.org/2003/05/soap-envelope/"},{"prefix": "urn","url": "urn:enterprise.soap.sforce.com"}]
*/
interface Envelope {
    "soapenv_Header": {
        "urn_SessionHeader": {
            "urn_sessionId": string
        }
    }
    "soapenv_Body": {
        "urn_convertLead": {
            "urn_leadConverts": {
                "urn_convertedStatus": string
                "urn_leadId": string
                "urn_opportunityName": string

            }[]
            "urn_overwriteLeadSource": boolean
            "urn_sendNotificationEmail": boolean
        }
    }
}



/**
 * functionName : map_S_root_S_Envelope
 * inputVariable : inputroot
*/
export function mapFunction(input: Root): Envelope {
    var convertedStatus = "true";
    return {
        "soapenv_Body": {
            "urn_convertLead": {
                "urn_leadConverts": input.lead
                    .map((leadItem) => {
                        return {
                            "urn_convertedStatus": convertedStatus,
                            "urn_leadId": leadItem.ID,
                            "urn_opportunityName": leadItem.name
                        }
                    }),
                "urn_overwriteLeadSource": dmUtils.toBoolean(input.overwriteLeadSource),
                "urn_sendNotificationEmail": dmUtils.toBoolean(input.sendNotificationEmail)
            }
        },
        "soapenv_Header": {
            "urn_SessionHeader": {
                "urn_sessionId": input.sessionId
            }
        }
    }
}

