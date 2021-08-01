(function(e){function t(t){for(var o,a,s=t[0],c=t[1],d=t[2],u=0,p=[];u<s.length;u++)a=s[u],Object.prototype.hasOwnProperty.call(r,a)&&r[a]&&p.push(r[a][0]),r[a]=0;for(o in c)Object.prototype.hasOwnProperty.call(c,o)&&(e[o]=c[o]);l&&l(t);while(p.length)p.shift()();return i.push.apply(i,d||[]),n()}function n(){for(var e,t=0;t<i.length;t++){for(var n=i[t],o=!0,s=1;s<n.length;s++){var c=n[s];0!==r[c]&&(o=!1)}o&&(i.splice(t--,1),e=a(a.s=n[0]))}return e}var o={},r={app:0},i=[];function a(t){if(o[t])return o[t].exports;var n=o[t]={i:t,l:!1,exports:{}};return e[t].call(n.exports,n,n.exports,a),n.l=!0,n.exports}a.m=e,a.c=o,a.d=function(e,t,n){a.o(e,t)||Object.defineProperty(e,t,{enumerable:!0,get:n})},a.r=function(e){"undefined"!==typeof Symbol&&Symbol.toStringTag&&Object.defineProperty(e,Symbol.toStringTag,{value:"Module"}),Object.defineProperty(e,"__esModule",{value:!0})},a.t=function(e,t){if(1&t&&(e=a(e)),8&t)return e;if(4&t&&"object"===typeof e&&e&&e.__esModule)return e;var n=Object.create(null);if(a.r(n),Object.defineProperty(n,"default",{enumerable:!0,value:e}),2&t&&"string"!=typeof e)for(var o in e)a.d(n,o,function(t){return e[t]}.bind(null,o));return n},a.n=function(e){var t=e&&e.__esModule?function(){return e["default"]}:function(){return e};return a.d(t,"a",t),t},a.o=function(e,t){return Object.prototype.hasOwnProperty.call(e,t)},a.p="/";var s=window["webpackJsonp"]=window["webpackJsonp"]||[],c=s.push.bind(s);s.push=t,s=s.slice();for(var d=0;d<s.length;d++)t(s[d]);var l=c;i.push([0,"chunk-vendors"]),n()})({0:function(e,t,n){e.exports=n("56d7")},"56d7":function(e,t,n){"use strict";n.r(t);n("e260"),n("e6cf"),n("cca6"),n("a79d");var o=n("2b0e"),r=function(){var e=this,t=e.$createElement,n=e._self._c||t;return n("router-view")},i=[],a=n("2877"),s={},c=Object(a["a"])(s,r,i,!1,null,null,null),d=c.exports,l=n("8c4f"),u=function(){var e=this,t=e.$createElement,n=e._self._c||t;return n("div",{attrs:{id:"container"}},[n("div",{attrs:{id:"wrapper"}},[n("div",{staticClass:"animate join",attrs:{id:"join"}},[n("h1",[e._v("Join a Room")]),n("form",{attrs:{"accept-charset":"UTF-8"},on:{submit:e.register}},[e._m(0),e._m(1),e._m(2)])]),e._m(3)])])},p=[function(){var e=this,t=e.$createElement,n=e._self._c||t;return n("p",[n("input",{attrs:{type:"text",name:"name",value:"",id:"name",placeholder:"Username",required:""}})])},function(){var e=this,t=e.$createElement,n=e._self._c||t;return n("p",[n("input",{attrs:{type:"text",name:"room",value:"",id:"roomName",placeholder:"Room",required:""}})])},function(){var e=this,t=e.$createElement,n=e._self._c||t;return n("p",{staticClass:"submit"},[n("input",{attrs:{type:"submit",name:"commit",value:"Join!"}})])},function(){var e=this,t=e.$createElement,n=e._self._c||t;return n("div",{staticStyle:{display:"none"},attrs:{id:"room"}},[n("h2",{attrs:{id:"room-header"}}),n("div",{attrs:{id:"participants"}}),n("input",{attrs:{type:"button",id:"button-leave",onmouseup:"leaveRoom();",value:"Leave room"}})])}],m=(n("b0c0"),n("159b"),n("fb6a"),"participant main"),f="participant";function h(e){this.name=e;var t=document.createElement("div");t.className=i()?f:m,t.id=e;var n=document.createElement("span"),o=document.createElement("video");function r(){if(t.className===f){var e=Array.prototype.slice.call(document.getElementsByClassName(m));e.forEach((function(e){e.className=f})),t.className=m}else t.className=f}function i(){return 0!=document.getElementsByClassName(m).length}t.appendChild(o),t.appendChild(n),t.onclick=r,document.getElementById("participants").appendChild(t),n.appendChild(document.createTextNode(e)),o.id="video-"+e,o.autoplay=!0,o.controls=!1,this.getElement=function(){return t},this.getVideoElement=function(){return o},this.offerToReceiveVideo=function(t,n,o){if(t)return console.error("sdp offer error");console.log("Invoking SDP offer callback function");var r={id:"receiveVideoFrom",sender:e,sdpOffer:n};sendMessage(r)},this.onIceCandidate=function(t,n){console.log("Local candidate"+JSON.stringify(t));var o={id:"onIceCandidate",candidate:t,name:e};sendMessage(o)},Object.defineProperty(this,"rtcPeer",{writable:!0}),this.dispose=function(){console.log("Disposing participant "+this.name),this.rtcPeer.dispose(),t.parentNode.removeChild(t)}}var v=n("d519"),g=n.n(v),b={name:"MeetingRoom",components:{},props:{},data:function(){return{ws:null,participants:null,name:null,room:null}},computed:{},created:function(){this.ws=new WebSocket("wss://"+location.host+"/groupcall"),window.onbeforeunload=function(){this.ws.close()},this.ws.onmessage=function(e){var t=JSON.parse(e.data);switch(console.info("Received message: "+e.data),t.id){case"existingParticipants":this.onExistingParticipants(t);break;case"newParticipantArrived":this.onNewParticipant(t);break;case"participantLeft":this.onParticipantLeft(t);break;case"receiveVideoAnswer":this.receiveVideoResponse(t);break;case"iceCandidate":this.participants[t.name].rtcPeer.addIceCandidate(t.candidate,(function(e){e&&console.error("Error adding candidate: "+e)}));break;default:console.error("Unrecognized message",t)}}},mounted:function(){},methods:{sendMessage:function(e){var t=JSON.stringify(e);console.log("Sending message: "+t),this.ws.send(t)},onParticipantLeft:function(e){console.log("Participant "+e.name+" left");var t=this.participants[e.name];t.dispose(),delete this.participants[e.name]},receiveVideo:function(e){var t=new h(e);this.participants[e]=t;var n=t.getVideoElement(),o={remoteVideo:n,onicecandidate:t.onIceCandidate.bind(t)};t.rtcPeer=new g.a.WebRtcPeer.WebRtcPeerRecvonly(o,(function(e){if(e)return console.error(e);this.generateOffer(t.offerToReceiveVideo.bind(t))}))},leaveRoom:function(){for(var e in this.sendMessage({id:"leaveRoom"}),this.participants)this.participants[e].dispose();document.getElementById("join").style.display="block",document.getElementById("room").style.display="none",this.ws.close()},onExistingParticipants:function(e){var t={audio:!0,video:{mandatory:{maxWidth:320,maxFrameRate:15,minFrameRate:15}}};console.log(this.name+" registered in room "+this.room);var n=new h(this.name);this.participants[this.name]=n;var o=n.getVideoElement(),r={localVideo:o,mediaConstraints:t,onicecandidate:n.onIceCandidate.bind(n)};n.rtcPeer=new g.a.WebRtcPeer.WebRtcPeerSendonly(r,(function(e){if(e)return console.error(e);this.generateOffer(n.offerToReceiveVideo.bind(n))})),e.data.forEach(this.receiveVideo)},callResponse:function(e){"accepted"!=e.response?(console.info("Call not accepted by peer. Closing call"),stop()):webRtcPeer.processAnswer(e.sdpAnswer,(function(e){if(e)return console.error(e)}))},receiveVideoResponse:function(e){this.participants[e.name].rtcPeer.processAnswer(e.sdpAnswer,(function(e){if(e)return console.error(e)}))},onNewParticipant:function(e){this.receiveVideo(e.name)},register:function(e){e.preventDefault(),this.name=document.getElementById("name").value,this.room=document.getElementById("roomName").value,document.getElementById("room-header").innerText="ROOM "+this.room,document.getElementById("join").style.display="none",document.getElementById("room").style.display="block";var t={id:"joinRoom",name:this.name,room:this.room};this.sendMessage(t)}}},y=b,w=Object(a["a"])(y,u,p,!1,null,null,null),P=w.exports;o["a"].use(l["a"]);var E=[{path:"/",name:"MeetingRoom",component:P}],R=new l["a"]({mode:"history",base:"/",routes:E}),_=R,O=n("2f62"),C={namespaced:!0,state:function(){return{}},mutations:{},actions:{},getters:{}};o["a"].use(O["a"]);var j=new O["a"].Store({state:{},mutations:{},actions:{},modules:{meetingRoom:C}});o["a"].config.productionTip=!1,new o["a"]({router:_,store:j,render:function(e){return e(d)}}).$mount("#app")}});
//# sourceMappingURL=app.6f7a5d21.js.map