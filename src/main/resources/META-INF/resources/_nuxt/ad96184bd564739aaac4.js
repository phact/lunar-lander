(window.webpackJsonp=window.webpackJsonp||[]).push([[4],{322:function(t,e,n){"use strict";n.r(e);n(78);var c,o=n(25),r=n(285),l=n.n(r),v={methods:{connect:(c=Object(o.a)(regeneratorRuntime.mark((function t(){var data;return regeneratorRuntime.wrap((function(t){for(;;)switch(t.prev=t.next){case 0:return t.next=2,l.a.post("/hello",{test:this.contactpoints});case 2:(data=t.sent).err||console.log(data);case 4:case"end":return t.stop()}}),t,this)}))),function(){return c.apply(this,arguments)})}},f=n(58),d=n(93),x=n.n(d),h=n(158),w=n(302),m=n(303),k=n(320),component=Object(f.a)(v,(function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("v-layout",[n("div",{staticClass:"text-center"},[n("logo")],1),t._v(" "),n("v-flex",{staticClass:"text-center"},[n("v-text-field",{attrs:{label:"Contact points",required:""},model:{value:t.contactpoints,callback:function(e){t.contactpoints=e},expression:"contactpoints"}}),t._v(" "),n("v-btn",{on:{click:function(e){return t.connect()}}},[t._v("Connect")])],1)],1)}),[],!1,null,null,null);e.default=component.exports;x()(component,{VBtn:h.a,VFlex:w.a,VLayout:m.a,VTextField:k.a})}}]);