/*! modernizr 3.3.1 (Custom Build) | MIT *
 * https://modernizr.com/download/?-csscalc-setclasses !*/
!function(e,n,s){function t(e,n){return typeof e===n}function o(){var e,n,s,o,a,i,c;for(var f in r)if(r.hasOwnProperty(f)){if(e=[],n=r[f],n.name&&(e.push(n.name.toLowerCase()),n.options&&n.options.aliases&&n.options.aliases.length))for(s=0;s<n.options.aliases.length;s++)e.push(n.options.aliases[s].toLowerCase());for(o=t(n.fn,"function")?n.fn():n.fn,a=0;a<e.length;a++)i=e[a],c=i.split("."),1===c.length?Modernizr[c[0]]=o:(!Modernizr[c[0]]||Modernizr[c[0]]instanceof Boolean||(Modernizr[c[0]]=new Boolean(Modernizr[c[0]])),Modernizr[c[0]][c[1]]=o),l.push((o?"":"no-")+c.join("-"))}}function a(e){var n=f.className,s=Modernizr._config.classPrefix||"";if(u&&(n=n.baseVal),Modernizr._config.enableJSClass){var t=new RegExp("(^|\\s)"+s+"no-js(\\s|$)");n=n.replace(t,"$1"+s+"js$2")}Modernizr._config.enableClasses&&(n+=" "+s+e.join(" "+s),u?f.className.baseVal=n:f.className=n)}function i(){return"function"!=typeof n.createElement?n.createElement(arguments[0]):u?n.createElementNS.call(n,"http://www.w3.org/2000/svg",arguments[0]):n.createElement.apply(n,arguments)}var l=[],r=[],c={_version:"3.3.1",_config:{classPrefix:"",enableClasses:!0,enableJSClass:!0,usePrefixes:!0},_q:[],on:function(e,n){var s=this;setTimeout(function(){n(s[e])},0)},addTest:function(e,n,s){r.push({name:e,fn:n,options:s})},addAsyncTest:function(e){r.push({name:null,fn:e})}},Modernizr=function(){};Modernizr.prototype=c,Modernizr=new Modernizr;var f=n.documentElement,u="svg"===f.nodeName.toLowerCase(),p=c._config.usePrefixes?" -webkit- -moz- -o- -ms- ".split(" "):["",""];c._prefixes=p,Modernizr.addTest("csscalc",function(){var e="width:",n="calc(10px);",s=i("a");return s.style.cssText=e+p.join(n+e),!!s.style.length}),o(),a(l),delete c.addTest,delete c.addAsyncTest;for(var m=0;m<Modernizr._q.length;m++)Modernizr._q[m]();e.Modernizr=Modernizr}(window,document);

function getContextPath(){
    var offset=location.href.indexOf(location.host)+location.host.length;
    var ctxPath=location.href.substring(offset,location.href.indexOf('/',offset+1));
    return ctxPath;
}

$(function(){
	var height = $(".container").css("height");
	$(".no-csscalc .container").css("min-height",$(window).height() - height);

	var height2 = $(".scroll-area").css("height");
	$(".no-csscalc .scroll-area").css("min-height",$(window).height() - height - height2);

	$(window).resize(function(){
		$(".no-csscalc .container").css("min-height",$(window).height() - height);
		$(".no-csscalc .scroll-area").css("min-height",($(window).height() - height - height2) + 'px');
	});
	
	$(".dep3").prev("a").append("<img src='" + $("#RESOURCES_PATH").val() + "/images/common/side-menu-arrow.png' alt=''>").removeAttr("href").bind("click keydown", function(){
		// 현재 선택되어 진입한 메뉴 이외에는 on 토글하지 않는다.
		$(this).toggleClass("expanded");
		$(this).next(".dep3").slideToggle();
		
		$(".dep3").prev("a").not(this).each(function(){
        	$(this).next(".dep3").slideUp();
        	$(this).removeClass("expanded");
        });
	        
	});
});
