<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.util.Enumeration" %>
<%


Enumeration e = request.getParameterNames();
while (e.hasMoreElements()) {
	String  object = (String ) e.nextElement();
	System.out.println(object + ":::" + request.getParameter(object));
	
}

System.out.println("aaaaaaaaaaaaa");
if (request.getParameter("root").equals("source")){
%>
[
	{
		"text": "1. Pre Lunch (120 min)",
		"expanded": true,
		"classes": "important",
		"children":
		[
			{
				"text": "1.1 The State of the Powerdome (30 min)"
			},
		 	{
				"text": "1.2 The Future of jQuery (30 min)"
			},
		 	{
				"text": "1.2 jQuery UI - A step to richnessy (60 min)"
			}
		]
	},
	{
		"text": "2. Lunch  (60 min)"
	},
	{
		"text": "3. After Lunch  (120+ min)",
		"expanded": false,
		"classes": "important",
		"children":
		[
			{
				"text": "3.1 jQuery Calendar Success Story (20 min)"
			},
		 	{
				"text": "3.2 jQuery and Ruby Web Frameworks (20 min)"
			},
		 	{
				"text": "3.3 Hey, I Can Do That! (20 min)"
			},
		 	{
				"text": "3.4 Taconite and Form (20 min)"
			},
		 	{
				"text": "3.5 Server-side JavaScript with jQuery and AOLserver (20 min)"
			},
		 	{
				"text": "3.6 The Onion: How to add features without adding features (20 min)",
				"id": "36",
				"classes": "important",
				"hasChildren": true
			},
		 	{
				"text": "3.7 Visualizations with JavaScript and Canvas (20 min)"
			},
		 	{
				"text": "3.8 ActiveDOM (20 min)"
			},
		 	{
				"text": "3.8 Growing jQuery (20 min)"
			}
		]
	}
]
<%
} else {
%>
[
	{
		"text": "1. Review of existing structures"
	},
	{
		"text": "2. Wrapper plugins"
	},
	{
		"text": "3. Summary"
	},
	{
		"text": "4. Questions and answers"
	},
	{
		"text": "3.6 The Onion: How to add features without adding features (20 min)",
		"id": "36",
		"classes": "important",
		"hasChildren": 1
	}
]
<%
}
%>