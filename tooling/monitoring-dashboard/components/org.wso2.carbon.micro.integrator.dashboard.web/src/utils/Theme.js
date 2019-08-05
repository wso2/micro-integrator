/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import { darkBaseTheme, getMuiTheme, lightBaseTheme } from 'material-ui/styles/index';
import * as Colors from 'material-ui/styles/colors';

export const darkTheme = getMuiTheme(darkBaseTheme,
    {
        name: 'dark',
        palette: {
            primary1Color: Colors.orange800,
            accent1Color: Colors.teal500,
            textColor: Colors.white,
            alternateTextColor: Colors.white,
        },
        appBar: {
            color: Colors.blueGrey900,
            textColor: Colors.grey200,
            height: 40,
        },
        drawer: {
            color: Colors.blueGrey900,
        },
        snackbar: {
            textColor: Colors.black,
        },
    }
);

export const lightTheme = getMuiTheme(lightBaseTheme,
    {
        name: 'light',
        palette: {
            primary1Color: Colors.orange800,
            accent1Color: Colors.teal500,
            textColor: Colors.black,
            alternateTextColor: Colors.black,
        },
        appBar: {
            color: Colors.grey200,
            textColor: Colors.grey900,
            height: 40,
        },
        drawer: {
            color: Colors.grey400,
        },
        snackbar: {
            textColor: Colors.white,
        },
        toggle: {
            trackOffColor: Colors.grey500,
            thumbOffColor: Colors.grey700,
        },
    }
);

export default darkTheme;
