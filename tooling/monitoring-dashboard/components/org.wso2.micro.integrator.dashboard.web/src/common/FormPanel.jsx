/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

import React, {Component} from "react";
import {darkBaseTheme, getMuiTheme} from 'material-ui/styles';
import PropTypes from 'prop-types'
//import 'styles/dashboard.css';

const muiTheme = getMuiTheme(darkBaseTheme);
const titleStyles = {
    fontFamily: muiTheme.fontFamily,
    color: muiTheme.palette.textColor
};

export default class FormPanel extends Component {

    render() {
        let wrapperStyles = {
            margin: '0 auto',
            width: Number(this.props.width),
            paddingTop: this.props.paddingTop,
            paddingBottom: 15,
            paddingTop: '100px'
        };

        return (
            <div style={wrapperStyles}>
                <form method="post" onSubmit={this.props.onSubmit}>
                    <h1 style={titleStyles}>{this.props.title}</h1>
                    {this.props.children}
                </form>
            </div>
        );
    }
}

FormPanel.propTypes = {
    title: PropTypes.string,
    onSubmit: PropTypes.func,
    width: PropTypes.number,
    paddingTop: PropTypes.number
};

FormPanel.defaultProps = {
    title: '',
    width: 400,
    paddingTop: 30
};
