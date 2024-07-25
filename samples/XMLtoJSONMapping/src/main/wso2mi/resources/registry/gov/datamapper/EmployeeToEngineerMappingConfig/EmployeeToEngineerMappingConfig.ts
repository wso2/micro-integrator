import * as dmUtils from "./dm-utils";

/*
* title : "ns1:employees",
* inputType : "XML",
* namespaces :[{"prefix": "ns1","url": "http://wso2.employee.info"},{"prefix": "ns2","url": "http://wso2.employee.address"}]
*/
interface Employees {
    "ns1_employee": {
        "ns1_firstname": string
        "ns1_lastname": string
        "ns2_addresses": {
            "ns2_address": {
                "ns2_city": {
                    attr_postalcode: number,
                    _ELEMVAL: string
                }
                "ns2_road": string
                attr_location: string
            }[]
        }
    }[]
}

/*
* title : "root",
* outputType : "JSON",
*/
interface Root {
    engineers: {
        engineerList: {
            fullname: string
            addresses: {
                address: {
                    location: string
                    city: {
                        postalcode: number
                        name: string
                    }
                    road: string
                }[]
            }
        }[]
    }
}



/**
 * functionName : map_S_employees_S_root
 * inputVariable : inputns1_employees
*/
export function mapFunction(input: Employees): Root {
    return {
        engineers: {
            engineerList: input["ns1_employee"]
                .map((ns1_employeeItem) => {
                    return {
                        addresses: {
                            address: ns1_employeeItem["ns2_addresses"]["ns2_address"]
                                .map((ns2_addressItem) => {
                                    return {
                                        road: ns2_addressItem["ns2_road"],
                                        city: {
                                            name: ns2_addressItem["ns2_city"]._ELEMVAL,
                                            postalcode: ns2_addressItem["ns2_city"].attr_postalcode
                                        },
                                        location: dmUtils.toUppercase(ns2_addressItem.attr_location)
                                    }
                                })
                        },
                        fullname: dmUtils.concat(ns1_employeeItem.ns1_firstname, " ", ns1_employeeItem.ns1_lastname)
                    }
                })
        }
    }
}

