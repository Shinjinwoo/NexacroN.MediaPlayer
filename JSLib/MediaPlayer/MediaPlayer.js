
//==============================================================================
//MediaPlayer
//==============================================================================

//==============================================================================
//nexacro.Event.MediaPlayerEventInfo
//MediaPlayer에 요청된 작업이 성공했을 때 발생되는 이벤트에서 사용되는 EventInfo Object
//==============================================================================

if(!nexacro.Event.MediaPlayerEventInfo)
{
    nexacro.Event.MediaPlayerEventInfo = function (strEventId, strSvcId, intReason, strReturnValue)
    {
        this.eventid = strEventId;                                              // 이벤트ID
        this.svcid = strSvcId;                                                  // 이벤트 서비스 ID
        this.reason = intReason;                                                // 이벤트 발생분류 코드
        this.returnvalue = strReturnValue;                                      // 이벤트 수행결과 (type:Variant)
    }
    _pMediaPlayerEventInfo = nexacro.Event.MediaPlayerEventInfo.prototype = nexacro._createPrototype(nexacro.Event);
    _pMediaPlayerEventInfo._type = "nexacroMediaPlayerEventInfo";
    _pMediaPlayerEventInfo._type_name = "MediaPlayerEventInfo";
    _pMediaPlayerEventInfo = null;
}

//==============================================================================
//nexacro.Event.MediaPlayerErrorEventInfo
//MediaPlayer에 요청된 작업이 실패했을 때 발생되는 이벤트에서 사용되는 EventInfo Object
//==============================================================================
if(!nexacro.Event.MediaPlayerErrorEventInfo)
{
    nexacro.Event.MediaPlayerErrorEventInfo = function (strEventId, strSvcId, intReason, intErrorCode, strErrorMsg)
    {
        this.eventid = strEventId;                                              // 이벤트ID
        this.svcid = strSvcId;                                                  // 이벤트 서비스 ID
        this.reason = intReason;
        this.errorcode = intErrorCode;
        this.errormsg = strErrorMsg;

    }
    _pMediaPlayerErrorEventInfo = nexacro.Event.MediaPlayerErrorEventInfo.prototype = nexacro._createPrototype(nexacro.Event);
    _pMediaPlayerErrorEventInfo._type = "nexacroMediaPlayerErrorEventInfo";
    _pMediaPlayerErrorEventInfo._type_name = "MediaPlayerErrorEventInfo";
    _pMediaPlayerErrorEventInfo = null;
}

//==============================================================================
//nexacro.MediaPlayer
//MediaPlayer를 연동하기 위해 사용한다.
//==============================================================================
if (!nexacro.MediaPlayer)
{
    nexacro.MediaPlayer = function(name, obj)
    {
        this._id = nexacro.Device.makeID();
        nexacro.Device._userCreatedObj[this._id] = this;
        this.name = name || "";

        this.enableevent = true;

        this.timeout = 10;

        this._clsnm = ["MediaPlayer"];
        this._reasoncode = {
            constructor : {ifcls: 0, fn: "constructor"},
            destroy     : {ifcls: 0, fn: "destroy"},

            callMethod  : {ifcls: 0, fn: "callMethod"},
        };

        this._event_list = {
            "oncallback": 1,
            "onpageload": 1,
        };

        // native constructor
        var params = {} ;
        var fninfo = this._reasoncode.constructor;
        this._execFn(fninfo, params);
    };

    var _pMediaPlayer = nexacro.MediaPlayer.prototype = nexacro._createPrototype(nexacro._EventSinkObject);

    _pMediaPlayer._type = "nexacroMediaPlayer";
    _pMediaPlayer._type_name = "MediaPlayer";

    _pMediaPlayer.destroy = function()
    {
        var params = {};
        var jsonstr;

        delete nexacro.Device._userCreatedObj[this._id];

        var fninfo = this._reasoncode.destroy;
        this._execFn(fninfo, params);
        return true;
    };

    //===================User Method=========================//
    _pMediaPlayer.callMethod = function(methodid, param)
    {
        var fninfo = this._reasoncode.callMethod;

        var params = {};

        params.serviceid = methodid;
        if(param === undefined || param == null) params.param = {};
        else params.param = param;

        this._execFn(fninfo, params);
    };

    //===================Native Call=========================//
    _pMediaPlayer._execFn = function(_obj, _param)
    {
        if(nexacro.Device.curDevice == 0)
        {
            var jsonstr = this._getJSONStr(_obj, _param);
            this._log(jsonstr);
            nexacro.Device.exec(jsonstr);
        }
        else
        {
            var jsonstr = this._getJSONStr(_obj, _param);
            this._log(jsonstr);
            nexacro.Device.exec(jsonstr);
        }
    }

    _pMediaPlayer._getJSONStr = function(_obj, _param)
    {
        var _id = this._id;
        var _clsnm = this._clsnm[_obj.ifcls];
        var _fnnm = _obj.fn;
        var value = {};
        value.id = _id;
        value.div = _clsnm;
        value.method = _fnnm;
        value.params = _param;

        return  JSON.stringify(value);
    }

    _pMediaPlayer._log = function(arg)
    {
        if(trace) {
            trace(arg);
        }
    }


    //===================EVENT=========================//
    _pMediaPlayer._oncallback = function(objData) {
        var e = new nexacro.Event.MediaPlayerEventInfo("oncallback", objData.svcid, objData.reason, objData.returnvalue);
        this.$fire_oncallback(this, e);
    };
    _pMediaPlayer.$fire_oncallback = function (objMediaPlayer, eMediaPlayerEventInfo) {
        if (this.oncallback && this.oncallback._has_handlers) {
            return this.oncallback._fireEvent(this, eMediaPlayerEventInfo);
        }
        return true;
    };

    _pMediaPlayer._onpageload = function(objData) {
        var e = new nexacro.Event.MediaPlayerEventInfo("onpageload", objData.svcid, objData.reason, objData.returnvalue);
        this.$fire_onpageload(this, e);
    };
    _pMediaPlayer.$fire_onpageload = function (objMediaPlayer, eMediaPlayerEventInfo) {
        if (this.onpageload && this.onpageload._has_handlers) {
            return this.onpageload._fireEvent(this, eMediaPlayerEventInfo);
        }
        return true;
    };

    delete _pMediaPlayer;
}