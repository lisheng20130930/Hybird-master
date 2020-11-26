/**
 * Created by Listen.Li on 2018/07/10.
 * History
 * 1. 2020/04/01 Listen.Li Upgrade UIWebView to WkWebView for iOS
 * 2. 2020/07/10 Listen.Li Native-Ajax for iOS < 11.0
 */
(function(win) {

    function getQueryString(name) {
        var reg = new RegExp('(^|&)' + name + '=([^&]*)(&|$)', 'i');
        var r = window.location.search.substr(1).match(reg);
        if (r !== null) return unescape(r[2]);
        return null;
    }

  function randomCode() {
    var data = ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
      "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L",
      "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X",
      "Y", "Z", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j",
      "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v",
      "w", "x", "y", "z"];
    var nums = "";
    for (var i = 0; i < 8; i++) {
      var r = Math.floor(Math.random() * 61);
      nums += data[r];
    }
    return nums;
  }

  function isAndroid() {
        return navigator.userAgent.indexOf('Android') > 0;
    }

    function isIOS() {
        return /(iPhone|iPad|iPod)/i.test(navigator.userAgent);
    }

    var mobile = {
        callAppRouter: function(method, params, callback) {
            var req = {
                'Method': method,
                'Data': params
            };
            if (isIOS() || isAndroid()) {
                webBridge.trace("before call router==>"+JSON.stringify(req));
                var cbName = 'CB_' + Date.now() + '_' + randomCode();
                win[cbName] = function(err, result) {
                    var resultObj = null;
                    var code = 0;
                    if (typeof(result) !== 'undefined' && result !== 'null' && result !== null) {
                        resultObj = JSON.parse(result)['result'];
                    }else{
                    	code = -1;
                    }
                    callback(code, resultObj);
                    delete win[cbName];
                };
                if (isAndroid()) {
                    win.bridge.callRouter(JSON.stringify(req), cbName);
                }else{
                    win.webkit.messageHandlers.callRouter.postMessage({"cbName":cbName,"reqStr":JSON.stringify(req)});
                }
            } else {
            		callback(-4, '{}');
            }
        },
        trace: function(str) {
            if (isAndroid()) {
                win.bridge.trace(str);
            }else if(isIOS()){
                win.webkit.messageHandlers.trace.postMessage(str);
            }else{
              console.log(str);
            }
        },
        onKeyback: function(callback){
            win['CB_onKeyback']=function(){
                if (0==callback()){
                    win.bridge.exitApp();
                }
            }
        },
        ajax: function(option,success,error){
		      this.callAppRouter('ajax', JSON.stringify(option), function(e, r) {
              if (e !== 0 || r['status']!==200) {
                  error((e!==0)?0:r['status']);
              } else {
                  success(r['res']);
              }
		    });
		},
		isAjaxEnabled: function(){
		    // Only iOS < 11.0 enabled native-Ajax
		    var enabled = false;
		    var ver = (navigator.appVersion).match(/OS (\d+)_(\d+)_?(\d+)?/);
		    if(null!=ver&&ver!='null'){ // iOS
		        ver = parseInt(ver[1],10);
		        enabled = (ver<11);
		    }
		    return enabled;
		}
    }
    win.webBridge = mobile;
})(window);
