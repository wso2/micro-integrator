/*
 * Copyright (c) 2022, WSO2 LLC (http://www.wso2.com).
 *
 * WSO2 LLC licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.micro.integrator.http.utils;

/**
 * This class contains the JSON payloads used in HTTP Core test cases.
 */
public class SamplePayloads {

    public static final String SMALL_PAYLOAD = "[\n" +
            "   {\n" +
            "      \"CLM_ID_VRSN_NUM\": \"1018-2204051\",\n" +
            "      \"CLM_ID\": 1018,\n" +
            "      \"CLM_VRSN_NUM\": 2204051,\n" +
            "      \"ADMIT_DT\": \"20211121\",\n" +
            "      \"CSTM_ITS_BLNG_PROV_ADD_TXT\": \"\",\n" +
            "      \"CSTM_ITS_BLNG_PROV_CTY_NM\": \"UNIONTOWN\",\n" +
            "      \"TOT_COINS_AMT\": 0,\n" +
            "      \"TOT_COPAY_AMT\": 0,\n" +
            "      \"TOT_DEDUCT_AMT\": 0,\n" +
            "      \"TOT_PD_AMT\": 0,\n" +
            "      \"CSTM_PROV_BLNG_NM\": \"UNIONTOWN HOSPITAL\",\n" +
            "      \"PTNT_GNDR_CD\": \"M\",\n" +
            "      \"AltpatID\": 34323,\n" +
            "      \"BLNG_PROV_ID\": \"000390041\",\n" +
            "      \"RNDR_PROV_NPI_ID\": \"1871594127\",\n" +
            "      \"RNDR_PROV_ID\": \"000390041\",\n" +
            "      \"Details\": [\n" +
            "         {\n" +
            "            \"SUBSCRIBER_SSN\": \"999-76-6866\",\n" +
            "            \"PTNT_BIRTH_DT\": \"19890525\",\n" +
            "            \"PTNT_NM\": \"Jose Gomez\",\n" +
            "            \"PTNT_FIRST_NAME\": \"Jose\",\n" +
            "            \"PTNT_LAST_NAME\": \"Gomez\",\n" +
            "            \"PAID_DATE\": \"20210303\",\n" +
            "            \"PAYER_ID\": \"TFS785644845-R57567565:649CE307-A670-48E1-A4E9-6CE95E18270A\",\n" +
            "            \"PAYER_NAME\": \"APCD1\",\n" +
            "            \"SUBMITTER_ID\": \"TFS785644845-R57567565:649CE307-A670-48E1-A4E9-6CE95E18270A\",\n" +
            "            \"SVC_LOC_STATE\": \"IN\",\n" +
            "            \"SVC_LOC_ZIP\": \"46020\",\n" +
            "            \"SVC_FACILITY_NPI\": 1578588463,\n" +
            "            \"BILL_PROV_NPI\": 1134124779,\n" +
            "            \"EXTERNAL_ID\": \"1d604da9-9a81-4ba9-80c2-de3375d59b40\"\n" +
            "         }\n" +
            "      ],\n" +
            "      \"Services\": [\n" +
            "         {\n" +
            "            \"CLM_ID_VRSN_NUM\": \"1018-2204051\",\n" +
            "            \"SRVC_FROM_DT\": \"20210428\",\n" +
            "            \"SRVC_THRU_DT\": \"20210428\",\n" +
            "            \"PRIN_ICD10_DIAG_CD\": \"E8352\",\n" +
            "            \"PROC_CD\": \"80053\",\n" +
            "            \"DIAG_DESC\": \"Hypocalcemia1\"\n" +
            "         },\n" +
            "         {\n" +
            "            \"CLM_ID_VRSN_NUM\": \"1018-2204051\",\n" +
            "            \"SRVC_FROM_DT\": \"20210428\",\n" +
            "            \"SRVC_THRU_DT\": \"20210428\",\n" +
            "            \"PRIN_ICD10_DIAG_CD\": \"E8352\",\n" +
            "            \"PROC_CD\": \"82306\",\n" +
            "            \"DIAG_DESC\": \"Hypocalcemia2\"\n" +
            "         },\n" +
            "         {\n" +
            "            \"CLM_ID_VRSN_NUM\": \"1018-2204051\",\n" +
            "            \"SRVC_FROM_DT\": \"20210428\",\n" +
            "            \"SRVC_THRU_DT\": \"20210428\",\n" +
            "            \"PRIN_ICD10_DIAG_CD\": \"E8352\",\n" +
            "            \"PROC_CD\": \"85025\",\n" +
            "            \"DIAG_DESC\": \"Hypocalcemia3\"\n" +
            "         },\n" +
            "         {\n" +
            "            \"CLM_ID_VRSN_NUM\": \"1018-2204051\",\n" +
            "            \"SRVC_FROM_DT\": \"20210428\",\n" +
            "            \"SRVC_THRU_DT\": \"20210428\",\n" +
            "            \"PRIN_ICD10_DIAG_CD\": \"E8352\",\n" +
            "            \"PROC_CD\": \"82652\",\n" +
            "            \"DIAG_DESC\": \"Hypocalcemia4\"\n" +
            "         }\n" +
            "      ]\n" +
            "   }\n" +
            "]";

    public static final String LARGE_PAYLOAD = "[\n" +
            "    {\n" +
            "      \"CLM_ID_VRSN_NUM\": \"2489651045\",\n" +
            "      \"type\": \"CreateEvent\",\n" +
            "      \"actor\": {\n" +
            "        \"id\": 665991,\n" +
            "        \"login\": \"petroav\",\n" +
            "        \"gravatar_id\": \"\",\n" +
            "        \"url\": \"https://api.github.com/users/petroav\",\n" +
            "        \"avatar_url\": \"https://avatars.githubusercontent.com/u/665991?\"\n" +
            "      },\n" +
            "      \"repo\": {\n" +
            "        \"id\": 28688495,\n" +
            "        \"name\": \"petroav/6.828\",\n" +
            "        \"url\": \"https://api.github.com/repos/petroav/6.828\"\n" +
            "      },\n" +
            "      \"payload\": {\n" +
            "        \"ref\": \"master\",\n" +
            "        \"ref_type\": \"branch\",\n" +
            "        \"master_branch\": \"master\",\n" +
            "        \"description\": \"Solution to homework and assignments from MIT's 6.828 (Operating Systems Engineering). Done in my spare time.\",\n" +
            "        \"pusher_type\": \"user\"\n" +
            "      },\n" +
            "      \"public\": true,\n" +
            "      \"created_at\": \"2015-01-01T15:00:00Z\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"2489651051\",\n" +
            "      \"type\": \"PushEvent\",\n" +
            "      \"actor\": {\n" +
            "        \"id\": 3854017,\n" +
            "        \"login\": \"rspt\",\n" +
            "        \"gravatar_id\": \"\",\n" +
            "        \"url\": \"https://api.github.com/users/rspt\",\n" +
            "        \"avatar_url\": \"https://avatars.githubusercontent.com/u/3854017?\"\n" +
            "      },\n" +
            "      \"repo\": {\n" +
            "        \"id\": 28671719,\n" +
            "        \"name\": \"rspt/rspt-theme\",\n" +
            "        \"url\": \"https://api.github.com/repos/rspt/rspt-theme\"\n" +
            "      },\n" +
            "      \"payload\": {\n" +
            "        \"push_id\": 536863970,\n" +
            "        \"size\": 1,\n" +
            "        \"distinct_size\": 1,\n" +
            "        \"ref\": \"refs/heads/master\",\n" +
            "        \"head\": \"6b089eb4a43f728f0a594388092f480f2ecacfcd\",\n" +
            "        \"before\": \"437c03652caa0bc4a7554b18d5c0a394c2f3d326\",\n" +
            "        \"commits\": [\n" +
            "          {\n" +
            "            \"sha\": \"6b089eb4a43f728f0a594388092f480f2ecacfcd\",\n" +
            "            \"author\": {\n" +
            "              \"email\": \"5c682c2d1ec4073e277f9ba9f4bdf07e5794dabe@rspt.ch\",\n" +
            "              \"name\": \"rspt\"\n" +
            "            },\n" +
            "            \"message\": \"Fix main header height on mobile\",\n" +
            "            \"distinct\": true,\n" +
            "            \"url\": \"https://api.github.com/repos/rspt/rspt-theme/commits/6b089eb4a43f728f0a594388092f480f2ecacfcd\"\n" +
            "          }\n" +
            "        ]\n" +
            "      },\n" +
            "      \"public\": true,\n" +
            "      \"created_at\": \"2015-01-01T15:00:01Z\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"2489651053\",\n" +
            "      \"type\": \"PushEvent\",\n" +
            "      \"actor\": {\n" +
            "        \"id\": 6339799,\n" +
            "        \"login\": \"izuzero\",\n" +
            "        \"gravatar_id\": \"\",\n" +
            "        \"url\": \"https://api.github.com/users/izuzero\",\n" +
            "        \"avatar_url\": \"https://avatars.githubusercontent.com/u/6339799?\"\n" +
            "      },\n" +
            "      \"repo\": {\n" +
            "        \"id\": 28270952,\n" +
            "        \"name\": \"izuzero/xe-module-ajaxboard\",\n" +
            "        \"url\": \"https://api.github.com/repos/izuzero/xe-module-ajaxboard\"\n" +
            "      },\n" +
            "      \"payload\": {\n" +
            "        \"push_id\": 536863972,\n" +
            "        \"size\": 1,\n" +
            "        \"distinct_size\": 1,\n" +
            "        \"ref\": \"refs/heads/develop\",\n" +
            "        \"head\": \"ec819b9df4fe612bb35bf562f96810bf991f9975\",\n" +
            "        \"before\": \"590433109f221a96cf19ea7a7d9a43ca333e3b3e\",\n" +
            "        \"commits\": [\n" +
            "          {\n" +
            "            \"sha\": \"ec819b9df4fe612bb35bf562f96810bf991f9975\",\n" +
            "            \"author\": {\n" +
            "              \"email\": \"df05f55543db3c62cf64f7438018ec37f3605d3c@gmail.com\",\n" +
            "              \"name\": \"Eunsoo Lee\"\n" +
            "            },\n" +
            "            \"message\": \"20 sample\",\n" +
            "            \"distinct\": true,\n" +
            "            \"url\": \"https://api.github.com/repos/izuzero/xe-module-ajaxboard/commits/ec819b9df4fe612bb35bf562f96810bf991f9975\"\n" +
            "          }\n" +
            "        ]\n" +
            "      },\n" +
            "      \"public\": true,\n" +
            "      \"created_at\": \"2015-01-01T15:00:01Z\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"2489651057\",\n" +
            "      \"type\": \"WatchEvent\",\n" +
            "      \"actor\": {\n" +
            "        \"id\": 6894991,\n" +
            "        \"login\": \"SametSisartenep\",\n" +
            "        \"gravatar_id\": \"\",\n" +
            "        \"url\": \"https://api.github.com/users/SametSisartenep\",\n" +
            "        \"avatar_url\": \"https://avatars.githubusercontent.com/u/6894991?\"\n" +
            "      },\n" +
            "      \"repo\": {\n" +
            "        \"id\": 2871998,\n" +
            "        \"name\": \"visionmedia/debug\",\n" +
            "        \"url\": \"https://api.github.com/repos/visionmedia/debug\"\n" +
            "      },\n" +
            "      \"payload\": {\n" +
            "        \"action\": \"started\"\n" +
            "      },\n" +
            "      \"public\": true,\n" +
            "      \"created_at\": \"2015-01-01T15:00:03Z\",\n" +
            "      \"org\": {\n" +
            "        \"id\": 9285252,\n" +
            "        \"login\": \"visionmedia\",\n" +
            "        \"gravatar_id\": \"\",\n" +
            "        \"url\": \"https://api.github.com/orgs/visionmedia\",\n" +
            "        \"avatar_url\": \"https://avatars.githubusercontent.com/u/9285252?\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"2489651062\",\n" +
            "      \"type\": \"PushEvent\",\n" +
            "      \"actor\": {\n" +
            "        \"id\": 485033,\n" +
            "        \"login\": \"winterbe\",\n" +
            "        \"gravatar_id\": \"\",\n" +
            "        \"url\": \"https://api.github.com/users/winterbe\",\n" +
            "        \"avatar_url\": \"https://avatars.githubusercontent.com/u/485033?\"\n" +
            "      },\n" +
            "      \"repo\": {\n" +
            "        \"id\": 28593843,\n" +
            "        \"name\": \"winterbe/streamjs\",\n" +
            "        \"url\": \"https://api.github.com/repos/winterbe/streamjs\"\n" +
            "      },\n" +
            "      \"payload\": {\n" +
            "        \"push_id\": 536863975,\n" +
            "        \"size\": 1,\n" +
            "        \"distinct_size\": 1,\n" +
            "        \"ref\": \"refs/heads/master\",\n" +
            "        \"head\": \"15b303203be31bd295bc831075da8f74b99b3981\",\n" +
            "        \"before\": \"0fef99f604154ccfe1d2fcd0aadeffb5c58e43ff\",\n" +
            "        \"commits\": [\n" +
            "          {\n" +
            "            \"sha\": \"15b303203be31bd295bc831075da8f74b99b3981\",\n" +
            "            \"author\": {\n" +
            "              \"email\": \"52a47bffd52d9cea1ee1362f2bd0c5f87fac9262@googlemail.com\",\n" +
            "              \"name\": \"Benjamin Winterberg\"\n" +
            "            },\n" +
            "            \"message\": \"Add comparator support for min, max operations\",\n" +
            "            \"distinct\": true,\n" +
            "            \"url\": \"https://api.github.com/repos/winterbe/streamjs/commits/15b303203be31bd295bc831075da8f74b99b3981\"\n" +
            "          }\n" +
            "        ]\n" +
            "      },\n" +
            "      \"public\": true,\n" +
            "      \"created_at\": \"2015-01-01T15:00:03Z\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"2489651063\",\n" +
            "      \"type\": \"PushEvent\",\n" +
            "      \"actor\": {\n" +
            "        \"id\": 4319954,\n" +
            "        \"login\": \"hermanwahyudi\",\n" +
            "        \"gravatar_id\": \"\",\n" +
            "        \"url\": \"https://api.github.com/users/hermanwahyudi\",\n" +
            "        \"avatar_url\": \"https://avatars.githubusercontent.com/u/4319954?\"\n" +
            "      },\n" +
            "      \"repo\": {\n" +
            "        \"id\": 27826205,\n" +
            "        \"name\": \"hermanwahyudi/selenium\",\n" +
            "        \"url\": \"https://api.github.com/repos/hermanwahyudi/selenium\"\n" +
            "      },\n" +
            "      \"payload\": {\n" +
            "        \"push_id\": 536863976,\n" +
            "        \"size\": 1,\n" +
            "        \"distinct_size\": 0,\n" +
            "        \"ref\": \"refs/heads/master\",\n" +
            "        \"head\": \"1b58dd4c4e14ea9cf5212b981774bd448a266c3c\",\n" +
            "        \"before\": \"20b10e3a605bd177efff62f1130943774ac07bf3\",\n" +
            "        \"commits\": [\n" +
            "          {\n" +
            "            \"sha\": \"1b58dd4c4e14ea9cf5212b981774bd448a266c3c\",\n" +
            "            \"author\": {\n" +
            "              \"email\": \"2bb20d8a71fb7adbc1d6239cc9ff4130f26819dc@gmail.com\",\n" +
            "              \"name\": \"Herman\"\n" +
            "            },\n" +
            "            \"message\": \"Update README.md\",\n" +
            "            \"distinct\": false,\n" +
            "            \"url\": \"https://api.github.com/repos/hermanwahyudi/selenium/commits/1b58dd4c4e14ea9cf5212b981774bd448a266c3c\"\n" +
            "          }\n" +
            "        ]\n" +
            "      },\n" +
            "      \"public\": true,\n" +
            "      \"created_at\": \"2015-01-01T15:00:03Z\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"2489651064\",\n" +
            "      \"type\": \"PushEvent\",\n" +
            "      \"actor\": {\n" +
            "        \"id\": 2881602,\n" +
            "        \"login\": \"jdilt\",\n" +
            "        \"gravatar_id\": \"\",\n" +
            "        \"url\": \"https://api.github.com/users/jdilt\",\n" +
            "        \"avatar_url\": \"https://avatars.githubusercontent.com/u/2881602?\"\n" +
            "      },\n" +
            "      \"repo\": {\n" +
            "        \"id\": 28682546,\n" +
            "        \"name\": \"jdilt/jdilt.github.io\",\n" +
            "        \"url\": \"https://api.github.com/repos/jdilt/jdilt.github.io\"\n" +
            "      },\n" +
            "      \"payload\": {\n" +
            "        \"push_id\": 536863977,\n" +
            "        \"size\": 1,\n" +
            "        \"distinct_size\": 1,\n" +
            "        \"ref\": \"refs/heads/master\",\n" +
            "        \"head\": \"d13cbd1e5c68b189fc91cfa14fdae1f52ef6f9e1\",\n" +
            "        \"before\": \"8515c4a9efb40332659e4389821a73800ce6a4bf\",\n" +
            "        \"commits\": [\n" +
            "          {\n" +
            "            \"sha\": \"d13cbd1e5c68b189fc91cfa14fdae1f52ef6f9e1\",\n" +
            "            \"author\": {\n" +
            "              \"email\": \"3e9bbe622d800410f1d4d0a4bb92004e147f1b1e@163.com\",\n" +
            "              \"name\": \"jdilt\"\n" +
            "            },\n" +
            "            \"message\": \"refine index page and about page\",\n" +
            "            \"distinct\": true,\n" +
            "            \"url\": \"https://api.github.com/repos/jdilt/jdilt.github.io/commits/d13cbd1e5c68b189fc91cfa14fdae1f52ef6f9e1\"\n" +
            "          }\n" +
            "        ]\n" +
            "      },\n" +
            "      \"public\": true,\n" +
            "      \"created_at\": \"2015-01-01T15:00:03Z\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"2489651066\",\n" +
            "      \"type\": \"PushEvent\",\n" +
            "      \"actor\": {\n" +
            "        \"id\": 3495129,\n" +
            "        \"login\": \"sundaymtn\",\n" +
            "        \"gravatar_id\": \"\",\n" +
            "        \"url\": \"https://api.github.com/users/sundaymtn\",\n" +
            "        \"avatar_url\": \"https://avatars.githubusercontent.com/u/3495129?\"\n" +
            "      },\n" +
            "      \"repo\": {\n" +
            "        \"id\": 24147122,\n" +
            "        \"name\": \"sundaymtn/waterline\",\n" +
            "        \"url\": \"https://api.github.com/repos/sundaymtn/waterline\"\n" +
            "      },\n" +
            "      \"payload\": {\n" +
            "        \"push_id\": 536863979,\n" +
            "        \"size\": 1,\n" +
            "        \"distinct_size\": 1,\n" +
            "        \"ref\": \"refs/heads/master\",\n" +
            "        \"head\": \"2a2ec35bfefb9341b1df2f213aad1dac804bc2ea\",\n" +
            "        \"before\": \"a7dba8faf22d2f342b7398ff76bfd10a30106191\",\n" +
            "        \"commits\": [\n" +
            "          {\n" +
            "            \"sha\": \"2a2ec35bfefb9341b1df2f213aad1dac804bc2ea\",\n" +
            "            \"author\": {\n" +
            "              \"email\": \"7fbc091194a9488bfb16868527a7c3a8ba469dba@gmail.com\",\n" +
            "              \"name\": \"Seth Carter\"\n" +
            "            },\n" +
            "            \"message\": \"Thu Jan  1 10:00:02 EST 2015\",\n" +
            "            \"distinct\": true,\n" +
            "            \"url\": \"https://api.github.com/repos/sundaymtn/waterline/commits/2a2ec35bfefb9341b1df2f213aad1dac804bc2ea\"\n" +
            "          }\n" +
            "        ]\n" +
            "      },\n" +
            "      \"public\": true,\n" +
            "      \"created_at\": \"2015-01-01T15:00:04Z\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"2489651067\",\n" +
            "      \"type\": \"PushEvent\",\n" +
            "      \"actor\": {\n" +
            "        \"id\": 10363514,\n" +
            "        \"login\": \"zhouzhi2015\",\n" +
            "        \"gravatar_id\": \"\",\n" +
            "        \"url\": \"https://api.github.com/users/zhouzhi2015\",\n" +
            "        \"avatar_url\": \"https://avatars.githubusercontent.com/u/10363514?\"\n" +
            "      },\n" +
            "      \"repo\": {\n" +
            "        \"id\": 28686619,\n" +
            "        \"name\": \"zhouzhi2015/temp\",\n" +
            "        \"url\": \"https://api.github.com/repos/zhouzhi2015/temp\"\n" +
            "      },\n" +
            "      \"payload\": {\n" +
            "        \"push_id\": 536863980,\n" +
            "        \"size\": 1,\n" +
            "        \"distinct_size\": 1,\n" +
            "        \"ref\": \"refs/heads/master\",\n" +
            "        \"head\": \"22019c081480435bb7d6e629766f2204c6c219bd\",\n" +
            "        \"before\": \"d5926ef8c6a8a43724f8dc94007c3c5a918391c3\",\n" +
            "        \"commits\": [\n" +
            "          {\n" +
            "            \"sha\": \"22019c081480435bb7d6e629766f2204c6c219bd\",\n" +
            "            \"author\": {\n" +
            "              \"email\": \"421c4f4cb8c7fe07ea1166286558dc42a56cf3a7\",\n" +
            "              \"name\": \"1184795629@qq.com\"\n" +
            "            },\n" +
            "            \"message\": \"测测\",\n" +
            "            \"distinct\": true,\n" +
            "            \"url\": \"https://api.github.com/repos/zhouzhi2015/temp/commits/22019c081480435bb7d6e629766f2204c6c219bd\"\n" +
            "          }\n" +
            "        ]\n" +
            "      },\n" +
            "      \"public\": true,\n" +
            "      \"created_at\": \"2015-01-01T15:00:04Z\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"2489651071\",\n" +
            "      \"type\": \"ReleaseEvent\",\n" +
            "      \"actor\": {\n" +
            "        \"id\": 7659931,\n" +
            "        \"login\": \"petrkutalek\",\n" +
            "        \"gravatar_id\": \"\",\n" +
            "        \"url\": \"https://api.github.com/users/petrkutalek\",\n" +
            "        \"avatar_url\": \"https://avatars.githubusercontent.com/u/7659931?\"\n" +
            "      },\n" +
            "      \"repo\": {\n" +
            "        \"id\": 20029610,\n" +
            "        \"name\": \"petrkutalek/png2pos\",\n" +
            "        \"url\": \"https://api.github.com/repos/petrkutalek/png2pos\"\n" +
            "      },\n" +
            "      \"payload\": {\n" +
            "        \"action\": \"published\",\n" +
            "        \"release\": {\n" +
            "          \"url\": \"https://api.github.com/repos/petrkutalek/png2pos/releases/818676\",\n" +
            "          \"assets_url\": \"https://api.github.com/repos/petrkutalek/png2pos/releases/818676/assets\",\n" +
            "          \"upload_url\": \"https://uploads.github.com/repos/petrkutalek/png2pos/releases/818676/assets{?name}\",\n" +
            "          \"html_url\": \"https://github.com/petrkutalek/png2pos/releases/tag/v1.5.4\",\n" +
            "          \"id\": 818676,\n" +
            "          \"tag_name\": \"v1.5.4\",\n" +
            "          \"target_commitish\": \"master\",\n" +
            "          \"name\": \"\",\n" +
            "          \"draft\": false,\n" +
            "          \"author\": {\n" +
            "            \"login\": \"petrkutalek\",\n" +
            "            \"id\": 7659931,\n" +
            "            \"avatar_url\": \"https://avatars.githubusercontent.com/u/7659931?v=3\",\n" +
            "            \"gravatar_id\": \"\",\n" +
            "            \"url\": \"https://api.github.com/users/petrkutalek\",\n" +
            "            \"html_url\": \"https://github.com/petrkutalek\",\n" +
            "            \"followers_url\": \"https://api.github.com/users/petrkutalek/followers\",\n" +
            "            \"following_url\": \"https://api.github.com/users/petrkutalek/following{/other_user}\",\n" +
            "            \"gists_url\": \"https://api.github.com/users/petrkutalek/gists{/gist_id}\",\n" +
            "            \"starred_url\": \"https://api.github.com/users/petrkutalek/starred{/owner}{/repo}\",\n" +
            "            \"subscriptions_url\": \"https://api.github.com/users/petrkutalek/subscriptions\",\n" +
            "            \"organizations_url\": \"https://api.github.com/users/petrkutalek/orgs\",\n" +
            "            \"repos_url\": \"https://api.github.com/users/petrkutalek/repos\",\n" +
            "            \"events_url\": \"https://api.github.com/users/petrkutalek/events{/privacy}\",\n" +
            "            \"received_events_url\": \"https://api.github.com/users/petrkutalek/received_events\",\n" +
            "            \"type\": \"User\",\n" +
            "            \"site_admin\": false\n" +
            "          },\n" +
            "          \"prerelease\": false,\n" +
            "          \"created_at\": \"2015-01-01T14:56:44Z\",\n" +
            "          \"published_at\": \"2015-01-01T15:00:05Z\",\n" +
            "          \"assets\": [\n" +
            "            {\n" +
            "              \"url\": \"https://api.github.com/repos/petrkutalek/png2pos/releases/assets/362298\",\n" +
            "              \"id\": 362298,\n" +
            "              \"name\": \"png2pos-v1.5.4-linux.zip\",\n" +
            "              \"label\": null,\n" +
            "              \"uploader\": {\n" +
            "                \"login\": \"petrkutalek\",\n" +
            "                \"id\": 7659931,\n" +
            "                \"avatar_url\": \"https://avatars.githubusercontent.com/u/7659931?v=3\",\n" +
            "                \"gravatar_id\": \"\",\n" +
            "                \"url\": \"https://api.github.com/users/petrkutalek\",\n" +
            "                \"html_url\": \"https://github.com/petrkutalek\",\n" +
            "                \"followers_url\": \"https://api.github.com/users/petrkutalek/followers\",\n" +
            "                \"following_url\": \"https://api.github.com/users/petrkutalek/following{/other_user}\",\n" +
            "                \"gists_url\": \"https://api.github.com/users/petrkutalek/gists{/gist_id}\",\n" +
            "                \"starred_url\": \"https://api.github.com/users/petrkutalek/starred{/owner}{/repo}\",\n" +
            "                \"subscriptions_url\": \"https://api.github.com/users/petrkutalek/subscriptions\",\n" +
            "                \"organizations_url\": \"https://api.github.com/users/petrkutalek/orgs\",\n" +
            "                \"repos_url\": \"https://api.github.com/users/petrkutalek/repos\",\n" +
            "                \"events_url\": \"https://api.github.com/users/petrkutalek/events{/privacy}\",\n" +
            "                \"received_events_url\": \"https://api.github.com/users/petrkutalek/received_events\",\n" +
            "                \"type\": \"User\",\n" +
            "                \"site_admin\": false\n" +
            "              },\n" +
            "              \"content_type\": \"application/zip\",\n" +
            "              \"state\": \"uploaded\",\n" +
            "              \"size\": 37781,\n" +
            "              \"download_count\": 0,\n" +
            "              \"created_at\": \"2015-01-01T14:59:22Z\",\n" +
            "              \"updated_at\": \"2015-01-01T14:59:23Z\",\n" +
            "              \"browser_download_url\": \"https://github.com/petrkutalek/png2pos/releases/download/v1.5.4/png2pos-v1.5.4-linux.zip\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"url\": \"https://api.github.com/repos/petrkutalek/png2pos/releases/assets/362297\",\n" +
            "              \"id\": 362297,\n" +
            "              \"name\": \"png2pos-v1.5.4-linux.zip.asc\",\n" +
            "              \"label\": null,\n" +
            "              \"uploader\": {\n" +
            "                \"login\": \"petrkutalek\",\n" +
            "                \"id\": 7659931,\n" +
            "                \"avatar_url\": \"https://avatars.githubusercontent.com/u/7659931?v=3\",\n" +
            "                \"gravatar_id\": \"\",\n" +
            "                \"url\": \"https://api.github.com/users/petrkutalek\",\n" +
            "                \"html_url\": \"https://github.com/petrkutalek\",\n" +
            "                \"followers_url\": \"https://api.github.com/users/petrkutalek/followers\",\n" +
            "                \"following_url\": \"https://api.github.com/users/petrkutalek/following{/other_user}\",\n" +
            "                \"gists_url\": \"https://api.github.com/users/petrkutalek/gists{/gist_id}\",\n" +
            "                \"starred_url\": \"https://api.github.com/users/petrkutalek/starred{/owner}{/repo}\",\n" +
            "                \"subscriptions_url\": \"https://api.github.com/users/petrkutalek/subscriptions\",\n" +
            "                \"organizations_url\": \"https://api.github.com/users/petrkutalek/orgs\",\n" +
            "                \"repos_url\": \"https://api.github.com/users/petrkutalek/repos\",\n" +
            "                \"events_url\": \"https://api.github.com/users/petrkutalek/events{/privacy}\",\n" +
            "                \"received_events_url\": \"https://api.github.com/users/petrkutalek/received_events\",\n" +
            "                \"type\": \"User\",\n" +
            "                \"site_admin\": false\n" +
            "              },\n" +
            "              \"content_type\": \"text/plain\",\n" +
            "              \"state\": \"uploaded\",\n" +
            "              \"size\": 495,\n" +
            "              \"download_count\": 0,\n" +
            "              \"created_at\": \"2015-01-01T14:59:21Z\",\n" +
            "              \"updated_at\": \"2015-01-01T14:59:22Z\",\n" +
            "              \"browser_download_url\": \"https://github.com/petrkutalek/png2pos/releases/download/v1.5.4/png2pos-v1.5.4-linux.zip.asc\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"url\": \"https://api.github.com/repos/petrkutalek/png2pos/releases/assets/362299\",\n" +
            "              \"id\": 362299,\n" +
            "              \"name\": \"png2pos-v1.5.4-osx.zip\",\n" +
            "              \"label\": null,\n" +
            "              \"uploader\": {\n" +
            "                \"login\": \"petrkutalek\",\n" +
            "                \"id\": 7659931,\n" +
            "                \"avatar_url\": \"https://avatars.githubusercontent.com/u/7659931?v=3\",\n" +
            "                \"gravatar_id\": \"\",\n" +
            "                \"url\": \"https://api.github.com/users/petrkutalek\",\n" +
            "                \"html_url\": \"https://github.com/petrkutalek\",\n" +
            "                \"followers_url\": \"https://api.github.com/users/petrkutalek/followers\",\n" +
            "                \"following_url\": \"https://api.github.com/users/petrkutalek/following{/other_user}\",\n" +
            "                \"gists_url\": \"https://api.github.com/users/petrkutalek/gists{/gist_id}\",\n" +
            "                \"starred_url\": \"https://api.github.com/users/petrkutalek/starred{/owner}{/repo}\",\n" +
            "                \"subscriptions_url\": \"https://api.github.com/users/petrkutalek/subscriptions\",\n" +
            "                \"organizations_url\": \"https://api.github.com/users/petrkutalek/orgs\",\n" +
            "                \"repos_url\": \"https://api.github.com/users/petrkutalek/repos\",\n" +
            "                \"events_url\": \"https://api.github.com/users/petrkutalek/events{/privacy}\",\n" +
            "                \"received_events_url\": \"https://api.github.com/users/petrkutalek/received_events\",\n" +
            "                \"type\": \"User\",\n" +
            "                \"site_admin\": false\n" +
            "              },\n" +
            "              \"content_type\": \"application/zip\",\n" +
            "              \"state\": \"uploaded\",\n" +
            "              \"size\": 27891,\n" +
            "              \"download_count\": 0,\n" +
            "              \"created_at\": \"2015-01-01T14:59:30Z\",\n" +
            "              \"updated_at\": \"2015-01-01T14:59:32Z\",\n" +
            "              \"browser_download_url\": \"https://github.com/petrkutalek/png2pos/releases/download/v1.5.4/png2pos-v1.5.4-osx.zip\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"url\": \"https://api.github.com/repos/petrkutalek/png2pos/releases/assets/362300\",\n" +
            "              \"id\": 362300,\n" +
            "              \"name\": \"png2pos-v1.5.4-osx.zip.asc\",\n" +
            "              \"label\": null,\n" +
            "              \"uploader\": {\n" +
            "                \"login\": \"petrkutalek\",\n" +
            "                \"id\": 7659931,\n" +
            "                \"avatar_url\": \"https://avatars.githubusercontent.com/u/7659931?v=3\",\n" +
            "                \"gravatar_id\": \"\",\n" +
            "                \"url\": \"https://api.github.com/users/petrkutalek\",\n" +
            "                \"html_url\": \"https://github.com/petrkutalek\",\n" +
            "                \"followers_url\": \"https://api.github.com/users/petrkutalek/followers\",\n" +
            "                \"following_url\": \"https://api.github.com/users/petrkutalek/following{/other_user}\",\n" +
            "                \"gists_url\": \"https://api.github.com/users/petrkutalek/gists{/gist_id}\",\n" +
            "                \"starred_url\": \"https://api.github.com/users/petrkutalek/starred{/owner}{/repo}\",\n" +
            "                \"subscriptions_url\": \"https://api.github.com/users/petrkutalek/subscriptions\",\n" +
            "                \"organizations_url\": \"https://api.github.com/users/petrkutalek/orgs\",\n" +
            "                \"repos_url\": \"https://api.github.com/users/petrkutalek/repos\",\n" +
            "                \"events_url\": \"https://api.github.com/users/petrkutalek/events{/privacy}\",\n" +
            "                \"received_events_url\": \"https://api.github.com/users/petrkutalek/received_events\",\n" +
            "                \"type\": \"User\",\n" +
            "                \"site_admin\": false\n" +
            "              },\n" +
            "              \"content_type\": \"text/plain\",\n" +
            "              \"state\": \"uploaded\",\n" +
            "              \"size\": 495,\n" +
            "              \"download_count\": 0,\n" +
            "              \"created_at\": \"2015-01-01T14:59:30Z\",\n" +
            "              \"updated_at\": \"2015-01-01T14:59:33Z\",\n" +
            "              \"browser_download_url\": \"https://github.com/petrkutalek/png2pos/releases/download/v1.5.4/png2pos-v1.5.4-osx.zip.asc\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"tarball_url\": \"https://api.github.com/repos/petrkutalek/png2pos/tarball/v1.5.4\",\n" +
            "          \"zipball_url\": \"https://api.github.com/repos/petrkutalek/png2pos/zipball/v1.5.4\",\n" +
            "          \"body\": \"\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"public\": true,\n" +
            "      \"created_at\": \"2015-01-01T15:00:05Z\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"2489651077\",\n" +
            "      \"type\": \"PushEvent\",\n" +
            "      \"actor\": {\n" +
            "        \"id\": 4070158,\n" +
            "        \"login\": \"caleb-eades\",\n" +
            "        \"gravatar_id\": \"\",\n" +
            "        \"url\": \"https://api.github.com/users/caleb-eades\",\n" +
            "        \"avatar_url\": \"https://avatars.githubusercontent.com/u/4070158?\"\n" +
            "      },\n" +
            "      \"repo\": {\n" +
            "        \"id\": 20469468,\n" +
            "        \"name\": \"caleb-eades/MinecraftServers\",\n" +
            "        \"url\": \"https://api.github.com/repos/caleb-eades/MinecraftServers\"\n" +
            "      },\n" +
            "      \"payload\": {\n" +
            "        \"push_id\": 536863983,\n" +
            "        \"size\": 1,\n" +
            "        \"distinct_size\": 1,\n" +
            "        \"ref\": \"refs/heads/master\",\n" +
            "        \"head\": \"6ea9a1f5b0b3c4204272a5fe2587a5ee146c3a49\",\n" +
            "        \"before\": \"8e94c95939b8f7db4c085da258698f07ae2b9cf3\",\n" +
            "        \"commits\": [\n" +
            "          {\n" +
            "            \"sha\": \"6ea9a1f5b0b3c4204272a5fe2587a5ee146c3a49\",\n" +
            "            \"author\": {\n" +
            "              \"email\": \"5bbfe2c07a3ef0b22b72711a2edf1c023f6433c5@gmail.com\",\n" +
            "              \"name\": \"caleb-eades\"\n" +
            "            },\n" +
            "            \"message\": \"Auto Snapshot Server State\",\n" +
            "            \"distinct\": true,\n" +
            "            \"url\": \"https://api.github.com/repos/caleb-eades/MinecraftServers/commits/6ea9a1f5b0b3c4204272a5fe2587a5ee146c3a49\"\n" +
            "          }\n" +
            "        ]\n" +
            "      },\n" +
            "      \"public\": true,\n" +
            "      \"created_at\": \"2015-01-01T15:00:05Z\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"2489651078\",\n" +
            "      \"type\": \"WatchEvent\",\n" +
            "      \"actor\": {\n" +
            "        \"id\": 285289,\n" +
            "        \"login\": \"comcxx11\",\n" +
            "        \"gravatar_id\": \"\",\n" +
            "        \"url\": \"https://api.github.com/users/comcxx11\",\n" +
            "        \"avatar_url\": \"https://avatars.githubusercontent.com/u/285289?\"\n" +
            "      },\n" +
            "      \"repo\": {\n" +
            "        \"id\": 5569958,\n" +
            "        \"name\": \"phpsysinfo/phpsysinfo\",\n" +
            "        \"url\": \"https://api.github.com/repos/phpsysinfo/phpsysinfo\"\n" +
            "      },\n" +
            "      \"payload\": {\n" +
            "        \"action\": \"started\"\n" +
            "      },\n" +
            "      \"public\": true,\n" +
            "      \"created_at\": \"2015-01-01T15:00:05Z\",\n" +
            "      \"org\": {\n" +
            "        \"id\": 6797923,\n" +
            "        \"login\": \"phpsysinfo\",\n" +
            "        \"gravatar_id\": \"\",\n" +
            "        \"url\": \"https://api.github.com/orgs/phpsysinfo\",\n" +
            "        \"avatar_url\": \"https://avatars.githubusercontent.com/u/6797923?\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"2489651080\",\n" +
            "      \"type\": \"WatchEvent\",\n" +
            "      \"actor\": {\n" +
            "        \"id\": 1757814,\n" +
            "        \"login\": \"Soufien\",\n" +
            "        \"gravatar_id\": \"\",\n" +
            "        \"url\": \"https://api.github.com/users/Soufien\",\n" +
            "        \"avatar_url\": \"https://avatars.githubusercontent.com/u/1757814?\"\n" +
            "      },\n" +
            "      \"repo\": {\n" +
            "        \"id\": 25873041,\n" +
            "        \"name\": \"wasabeef/awesome-android-libraries\",\n" +
            "        \"url\": \"https://api.github.com/repos/wasabeef/awesome-android-libraries\"\n" +
            "      },\n" +
            "      \"payload\": {\n" +
            "        \"action\": \"started\"\n" +
            "      },\n" +
            "      \"public\": true,\n" +
            "      \"created_at\": \"2015-01-01T15:00:05Z\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"2489651083\",\n" +
            "      \"type\": \"PushEvent\",\n" +
            "      \"actor\": {\n" +
            "        \"id\": 9538449,\n" +
            "        \"login\": \"hcremers\",\n" +
            "        \"gravatar_id\": \"\",\n" +
            "        \"url\": \"https://api.github.com/users/hcremers\",\n" +
            "        \"avatar_url\": \"https://avatars.githubusercontent.com/u/9538449?\"\n" +
            "      },\n" +
            "      \"repo\": {\n" +
            "        \"id\": 26101634,\n" +
            "        \"name\": \"ktgw0316/LightZone-l10n-nl\",\n" +
            "        \"url\": \"https://api.github.com/repos/ktgw0316/LightZone-l10n-nl\"\n" +
            "      },\n" +
            "      \"payload\": {\n" +
            "        \"push_id\": 536863987,\n" +
            "        \"size\": 1,\n" +
            "        \"distinct_size\": 1,\n" +
            "        \"ref\": \"refs/heads/master\",\n" +
            "        \"head\": \"0fca01b12e6a8a1c537842d4831906d1eb4a277e\",\n" +
            "        \"before\": \"fe610605ba48a87ee7c9bcf1a8a8db5f51bc4b58\",\n" +
            "        \"commits\": [\n" +
            "          {\n" +
            "            \"sha\": \"0fca01b12e6a8a1c537842d4831906d1eb4a277e\",\n" +
            "            \"author\": {\n" +
            "              \"email\": \"8800578b51f022c8d8adb9606a8b3db4fedbdac6@192.168.0.167\",\n" +
            "              \"name\": \"hans\"\n" +
            "            },\n" +
            "            \"message\": \"Translated by hcremers\",\n" +
            "            \"distinct\": true,\n" +
            "            \"url\": \"https://api.github.com/repos/ktgw0316/LightZone-l10n-nl/commits/0fca01b12e6a8a1c537842d4831906d1eb4a277e\"\n" +
            "          }\n" +
            "        ]\n" +
            "      },\n" +
            "      \"public\": true,\n" +
            "      \"created_at\": \"2015-01-01T15:00:05Z\"\n" +
            "    }\n" +
            "  ]";
}
