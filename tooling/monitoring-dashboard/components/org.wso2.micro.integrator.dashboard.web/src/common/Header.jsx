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

import React, {Component} from 'react';
import {Link} from 'react-router-dom';
import PropTypes from 'prop-types';

import {AppBar} from 'material-ui';

import defaultTheme from '../utils/Theme';

import UserMenu from './UserMenu';

export default class Header extends Component {
    render() {
        return (
            <AppBar
                style={{zIndex: this.props.theme.zIndex.drawer + 100, position: 'fixed'}}
                title={this.props.title}
                iconElementRight={this.props.rightElement}
                iconElementLeft={this.props.logo}
                onLeftIconButtonClick={this.props.onLogoClick}
                iconStyleLeft={{margin: '0 15px 0 0', display: 'flex', alignItems: 'center'}}
                titleStyle={{fontSize: 16}}
                zDepth={2}
            />
        );
    }
}


Header.propTypes = {
    logo: PropTypes.element,
    onLogoClick: PropTypes.func,
    title: PropTypes.oneOfType([
        PropTypes.string,
        PropTypes.element,
    ]).isRequired,
    rightElement: PropTypes.element,
    theme: PropTypes.shape({}),
};

Header.defaultProps = {
    logo: (
        <Link style={{height: '17px'}} to={'/home'}>
            <img
                height='17'
                src={`${window.contextPath}/public/app/images/logo.svg`}
                alt='logo'
            />
        </Link>
    ),
    onLogoClick: null,
    rightElement: <UserMenu/>,
    theme: defaultTheme,
};
