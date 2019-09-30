/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import React, {Component} from 'react';
import Box from '@material-ui/core/Box';
import Typography from '@material-ui/core/Typography';
import PropTypes from 'prop-types';

const styles = {
    titleSection: {
        paddingLeft: "10px"
    },
    box: {
        width: '100%',
        color: '#ffffff',
        backgroundColor: '#273c79'
    }
};

export default class TableHeaderBox extends Component {


    render() {
        return (<Box bgcolor={this.props.bgColor} color={this.props.color} style={styles.box}>
            <Typography variant="h6" id="tableTitle" style={styles.titleSection}>
                {this.props.title}
            </Typography>
        </Box>);

    }
}

TableHeaderBox.propTypes = {
    title: PropTypes.string,
    bgColor: PropTypes.string,
    color: PropTypes.string
};

TableHeaderBox.defaultProps = {
    title: 'No title',
    bgColor: "primary.main",
    color: "primary.contrastText"
};