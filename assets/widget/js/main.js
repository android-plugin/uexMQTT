 window.uexOnload = function() {

     uexMQTT.onStatusChange = function(data) {
         document.getElementById("status").innerHTML = "status: " + data.status;
     }
     uexMQTT.onNewMessage = function(data) {
         alert("receive message!\nmid: " + data.mid + "\non topic: " + data.topic + "\nqos: " + data.qos + "\nretainFlag: " + data.retainFlag + "\ndata: " + data.data);
     }
     uexMQTT.cbConnect = function(data) {
         if (data.isSuccess) {
             alert("connect SUCCESS!");
         } else {
             alert("connect FAILED!errCode:" + data.errCode);
         }
     }
     uexMQTT.cbSubscribe = function(data) {
         if (data.isSuccess) {
             alert("subscribe topic: " + data.topic + " SUCCESS!\ngranted qoss: " + data.grantedQoss);
         } else {
             alert("subscribe topic: " + data.topic + " FAILED!\nerrCode: " + data.errCode);
         }
     }
     uexMQTT.cbUnsubscribe = function(data) {
         if (data.isSuccess) {
             alert("unsubscribe topic: " + data.topic + " SUCCESS!");
         } else {
             alert("unsubscribe topic: " + data.topic + " FAILED!\nerrCode: " + data.errCode);
         }
     }
     uexMQTT.cbPublish = function(data) {
         if (data.isSuccess) {
             alert("publish msg : " + data.id + "on topic: " + data.topic + " SUCCESS!");
         } else {
             alert("publish msg : " + data.id + "on topic: " + data.topic + " FAILED!\nerrCode: " + data.errCode);
         }
     }
 }

 // var test = function(){
 //   uexMQTT.test();
 // }
 var init = function() {
     uexMQTT.init();
 }
 var connect = function() {
     uexMQTT.connect({
         clientId: "111111", //String,可选,客户端Id,此参数不传时,将随机生成一个
         server: "120.26.77.175", //String,必选,服务器地址
         port: 1886, //Number,必选,服务器端口
         username: "testUser1", //String,可选,用户名(如果服务器允许匿名登录,此参数可为空)
         password: "123456", //String,可选,用户密码(传username时,此参数必传,不传username时,此参数将被忽略)
         keepAliveInterval: 30, //Number,必选,心跳包发送频率,单位:秒
         // LWT:{//Object,可选,Last Will and Testament相关设置
         //   enable:true,//Boolean, 是否启用LWT
         //   topic:"willTopic",//String,willMessage的topic
         //   qos:1,//Number,willMessage的qos
         //   data:"willData",//String,willMessage的data
         //   retainFlag:true,//Boolean,willMessage的retainFlag
         // }
     });
 }
 var subscribe = function() {
     uexMQTT.subscribe({
         topic: "b1e57467c92140e299022deb808cdd24/000000/get", //String,必选,要订阅的topic
         qos: 1, //Number,必选 此topic的qos
     });
 }
 var unsubscribe = function() {
     uexMQTT.unsubscribe({
         topic: "b1e57467c92140e299022deb808cdd24/000000/get", //String,必选,要订阅的topic
     });
 }
 var publish = function() {
     uexMQTT.publish({
         id: "uid123456", //String,必选,自定义id,用于在cbPublish中区分消息
         topic: "b1e57467c92140e299022deb808cdd24/000000/set", //String,必选,发布消息的topic
         qos: 0, //Number,必选,要发布消息的qos
         data: "heeello!", //String,必选,要发布的消息数据
         retainFlag: false //Boolean,可选. MQTT broker是否要保留此消息,默认false
     });
 }
 var disconnect = function() {
     uexMQTT.disconnect();
 }
