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
import PropTypes from 'prop-types';

import defaultTheme from '../utils/Theme';
import Header from './Header';
import SideDrawer from './SideDrawer';
import {MuiThemeProvider} from 'material-ui/styles';
import Paper from '@material-ui/core/Paper';

/**
 * Style constants.
 */
const styles = {
    contentPaper: {
        height: '100vh',
        width: 'calc(100% - 240px)',
        float: 'right',
        paddingTop: '40px',
    },
    footer:{

    },
};

export default class ListViewParent extends Component {

    /**
     * Render the general view of the synapse artifacts listing pages
     */
    renderParentView() {
        return (
            <MuiThemeProvider muiTheme={this.props.theme}>
                <Header
                    title={this.props.title} theme={this.props.theme}
                />
                <SideDrawer/>
                <Paper style={styles.contentPaper}>
                    <div>
                        {this.props.data}
                    </div>

                </Paper>

            </MuiThemeProvider>
        );
    }


    render() {
        return this.renderParentView();
    }
}

ListViewParent.propTypes = {
    title: PropTypes.string,
    theme: PropTypes.shape({}),
    data: PropTypes.element,

};

ListViewParent.defaultProps = {
    title: 'Micro Integrator',
    data: <span/>,
    theme: defaultTheme,
};