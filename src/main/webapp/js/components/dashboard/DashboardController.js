'use strict';

import React from 'react';
import injectIntl from '../../utils/injectIntl';

import Routing from '../../utils/Routing';
import Routes from '../../utils/Routes';
import RouterStore from '../../stores/RouterStore';
import Dashboard from './Dashboard';
import I18nMixin from '../../i18n/I18nMixin';

let DashboardController = React.createClass({
    mixins: [
        I18nMixin
    ],

    _showScripts: function () {
        Routing.transitionTo(Routes.scripts);
    },

    _showFunctions: function () {
        Routing.transitionTo(Routes.functions);
    },

    _showValidation: function () {
        Routing.transitionTo(Routes.validation);
    },

    render: function () {
        let handlers = {
            showScripts: this._showScripts,
            showFunctions: this._showFunctions,
            showValidation: this._showValidation
        };
        return <div>
            <Dashboard dashboard={this._resolveDashboard()} handlers={handlers}/>
        </div>;
    },

    _resolveDashboard: function () {
        let payload = RouterStore.getTransitionPayload(Routes.dashboard.name);
        RouterStore.setTransitionPayload(Routes.dashboard.name, null);
        return payload ? payload.dashboard : null;
    }
});

module.exports = injectIntl(DashboardController);