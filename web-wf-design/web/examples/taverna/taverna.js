/**
 * jsBox
 */
var jsBox = {
	   
   language: {
	
	   languageName: "tavernaWF",
	
		modules: [
                    {
			"name": "Echo",
			"category": "service",
			"description": "Echoes input to output",
			"container" : {
				"xtype":"TavernaWFContainer",
				"inputs": ["in"],
				"outputs": ["out"],
// 				"terminals": [
//						{"name": "in", "direction": [-1,0], "offsetPosition": {"left": 20, "top": -14},"ddConfig": {"type": "input","allowedTypes": ["output"]}, "nMaxWires": 1 },
//						{"name": "out", "direction": [1,0], "offsetPosition": {"left": 65, "top": 20 },"ddConfig": {"type": "output","allowedTypes": ["input"]}}
//					],
                                 "wfURI":"file://blah1"
                            }
                    },
                    {
			"name": "String concatenation",
			"category": "service",
			"description": "Concatenates two strings",
			"container" : {
				"xtype":"TavernaWFContainer",
// 				"terminals": [
//						{"name": "string1", "direction": [-1,0], "offsetPosition": {"left": 20, "top": -14 },"ddConfig": {"type": "input","allowedTypes": ["output"]}, "nMaxWires": 1 },
//						{"name": "string2", "direction": [-1,0], "offsetPosition": {"left": 80, "top": -14 },"ddConfig": {"type": "input","allowedTypes": ["output"]}, "nMaxWires": 1 },
//						{"name": "out", "direction": [1,0], "offsetPosition": {"left": 65, "top": 20 },"ddConfig": {"type": "output","allowedTypes": ["input"]}}
//					],
				"inputs": ["string1", "string2"],
				"outputs": ["result"],
                                "wfURI":"file://blah2"
                            }
                    },
//                    {
//			"name": "Output",
//			"category": "output",
//			"description": "Workflow output",
//			"container" : {
//				"xtype":"WireIt.InOutContainer",
//				"outputs": ["out"]
//			}
//                    },
                    {
                        "name": "Output",
			"category": "output",
			"description": "Workflow output",
                        "container": {
                            "xtype": "WireIt.FormContainer",
                            "title": "output",
                            "fields": [ 
                                {"type": "string", "inputParams": {"label": "Value", "name": "output_value", "wirable": false}}
                            ],
                        "terminals": [
                            {
                                "name": "in", "direction": [0,-1], "offsetPosition": {"left": 82, "top": -15 }, "ddConfig": {
                                    "type": "input",
                                    "allowedTypes": ["output"]
                                },
                                "nMaxWires": 1
                            }
                        ]
                        }
                    },
//                    {
//			"name": "Simple input",
//			"category": "input",
//			"description": "Simple workflow input",
//			"container" : {
//				"xtype":"WireIt.InOutContainer",
//				"inputs": ["in"]
//			}
//                    },
                    {
                        "name": "Simple input",
                        "category": "input",
                        "container": {
                            "xtype": "WireIt.FormContainer",
                            "title": "input",
                            "fields": [
                                {
                                    "type": "type",
                                    "inputParams":
                                        {
                                            "label": "Value",
                                            "name": "input_value",
                                            "wirable": false,
                                            "value": {
                                                "type":"string",
                                                "inputParams":{
                                                    "typeInvite": "insert value"
                                                }
                                            }
                                        }
                                }
                            ],
                            "terminals": [
                                {
                                    "name": "out",
                                    "direction": [0,1],
                                    "offsetPosition": {"left": 86, "bottom": -15},
                                    "ddConfig":
                                        {
                                            "type": "output",
                                            "allowedTypes": ["input"]
                                        }
                                }
                            ]
                        }
                    },
		   {
		      "name": "List Input",
                      "category": "input",
		      "container": {
		         "xtype": "WireIt.FormContainer",
		   		"title": "Input test",
		   		"fields": [{"type": "text", "inputParams": {"label": "List values", "name": "input_list", "wirable": false }} ],
                                "terminals": [
                                {
                                    "name": "out",
                                    "direction": [0,1],
                                    "offsetPosition": {"left": 86, "bottom": -15},
                                    "ddConfig":
                                        {
                                            "type": "output",
                                            "allowedTypes": ["input"]
                                        }
                                }
                            ]
                      }
//		      "value": {
//		         "input": {
//		            "type":"url","inputParams":{}
//		         }
//		      }
		   }
//                 {
//		      "name": "jsBox",
//		      "container": {"xtype": "jsBox.Container"}
//		   },
//
//		   {
//		      "name": "comment",
//		      "container": {
//		         "xtype": "WireIt.FormContainer",
//		   		"title": "Comment",
//		   		"fields": [
//		            {"type": "text", "inputParams": {"label": "", "name": "comment", "wirable": false }}
//		         ]
//		      },
//		      "value": {
//		         "input": {
//		            "type":"url","inputParams":{}
//		         }
//		      }
//		   },
//
//		   {
//		      "name": "callback",
//		      "container": {
//		         "xtype": "WireIt.Container",
//		         "title": "Callback",
//		         "terminals": [
//		            {
//		               "name": "callbackFunction",
//		               "direction": [0,1], "offsetPosition": {"left": 56, "bottom": -15}, "ddConfig": {
//                         "type": "output",
//                         "allowedTypes": ["input"]
//                      },
//                      "wireConfig":{"color": "#EEEE11", "bordercolor":"#FFFF00"}
//		            },
//		             {
//   		               "name": "output",
//   		               "direction": [0,1], "offsetPosition": {"left": 126, "bottom": -15}, "ddConfig": {
//                            "type": "output",
//                            "allowedTypes": ["input"]
//                         }
//   		            }
//		         ]
//		      }
//		   }
		]
	},
   
   /**
    * @method init
    * @static
    */
   init: function() {
	WireIt.WiringEditor.adapters.Ajax.config = 	{
		deleteWiring: {
			method: 'GET',
			url: function(value) {
				if(console && console.log) {
					console.log(value);
				}
				// for a REST query you might want to send a DELETE /resource/wirings/moduleName
//				return "fakeSaveDelete.json";
				return "../../DeleteWireit?name=" + value.name + "&language=" + value.language;
			}
		},
		saveWiring: {
//			method: 'GET',
//			url: 'fakeSaveDelete.json'
			method: 'POST',
			url: '../../SaveWireit'
		},
		listWirings: {
			method: 'GET',
			url: function(value) {
				if(console && console.log) {
					console.log(value);
				}
				// for a REST query you might want to send a DELETE /resource/wirings/moduleName
				return "../../ListWireit?language=" + value.language;
			}
		}
	};
	
	this.language.adapter = WireIt.WiringEditor.adapters.Ajax;
	
   	this.editor = new jsBox.WiringEditor(this.language);

	// Open the infos panel
	editor.accordionView.openPanel(2);
   },
   
   /**
    * Execute the module in the "ExecutionFrame" virtual machine
    * @method run
    * @static
    */
   run: function() {
      var ef = new ExecutionFrame( this.editor.getValue() );
      ef.run();
   }
   
};


/**
 * The wiring editor is overriden to add a button "RUN" to the control bar
 */
jsBox.WiringEditor = function(options) {
   jsBox.WiringEditor.superclass.constructor.call(this, options);
};

YAHOO.lang.extend(jsBox.WiringEditor, WireIt.ComposableWiringEditor, {
   /**
    * Add the "run" button
    */
   renderButtons: function() {
      jsBox.WiringEditor.superclass.renderButtons.call(this);

		// Add the run button to the toolbar
      var toolbar = YAHOO.util.Dom.get('toolbar');
      var runButton = new YAHOO.widget.Button({ label:"Run", id:"WiringEditor-runButton", container: toolbar });
      runButton.on("click", jsBox.run, jsBox, true);
   }
});



/**
 * Container class used by the "jsBox" module (automatically sets terminals depending on the number of arguments)
 * @class Container
 * @namespace jsBox
 * @constructor
 */
jsBox.Container = function(options, layer) {
         
   jsBox.Container.superclass.constructor.call(this, options, layer);
   
   this.buildTextArea(options.codeText || "function(e) {\n\n  return 0;\n}");
   
   this.createTerminals();
   
   // Reposition the terminals when the jsBox is being resized
   this.ddResize.eventResize.subscribe(function(e, args) {
      this.positionTerminals();
      YAHOO.util.Dom.setStyle(this.textarea, "height", (args[0][1]-70)+"px");
   }, this, true);
};

YAHOO.extend(jsBox.Container, WireIt.Container, {
   
   /**
    * Create the textarea for the javascript code
    * @method buildTextArea
    * @param {String} codeText
    */
   buildTextArea: function(codeText) {

      this.textarea = WireIt.cn('textarea', null, {width: "90%", height: "70px", border: "0", padding: "5px"}, codeText);
      this.setBody(this.textarea);

      YAHOO.util.Event.addListener(this.textarea, 'change', this.createTerminals, this, true);
      
   },
   
   /**
    * Create (and re-create) the terminals with this.nParams input terminals
    * @method createTerminals
    */
   createTerminals: function() {
      
      // Output Terminal
      if(!this.outputTerminal) {
   	   this.outputTerminal = this.addTerminal({xtype: "WireIt.util.TerminalOutput", "name": "out"});      
         this.outputTerminal.jsBox = this;
      }
      
      // Input terminals :
      var match = (this.textarea.value).match((/^[ ]*function[ ]*\((.*)\)[ ]*\{/));
   	var sParamList = match ? match[1] : "";
      var params = sParamList.split(',');
      var nParams = (sParamList=="") ? 0 : params.length;
      
      var curTerminalN = this.nParams || 0;
      if(curTerminalN < nParams) {
         // add terminals
         for(var i = curTerminalN ; i < nParams ; i++) {
            var term = this.addTerminal({xtype: "WireIt.util.TerminalInput", "name": "param"+i});
            //term.jsBox = this;
            WireIt.sn(term.el, null, {position: "absolute", top: "-15px"});
         }
      }
      else if (curTerminalN > nParams) {
         // remove terminals
         for(var i = this.terminals.length-(curTerminalN-nParams) ; i < this.terminals.length ; i++) {
         	this.terminals[i].remove();
         	this.terminals[i] = null;
         }
         this.terminals = WireIt.compact(this.terminals);
      }
      this.nParams = nParams;
   
      this.positionTerminals();

      // Declare the new terminals to the drag'n drop handler (so the wires are moved around with the container)
      this.dd.setTerminals(this.terminals);
   },
   
   /**
    * Reposition the terminals
    * @method positionTerminals
    */
   positionTerminals: function() {
      var width = WireIt.getIntStyle(this.el, "width");

      var inputsIntervall = Math.floor(width/(this.nParams+1));

      for(var i = 1 ; i < this.terminals.length ; i++) {
         var term = this.terminals[i];
         YAHOO.util.Dom.setStyle(term.el, "left", (inputsIntervall*(i))-15+"px" );
         for(var j = 0 ; j < term.wires.length ; j++) {
            term.wires[j].redraw();
         }
      }
      
      // Output terminal
      WireIt.sn(this.outputTerminal.el, null, {position: "absolute", bottom: "-15px", left: (Math.floor(width/2)-15)+"px"});
      for(var j = 0 ; j < this.outputTerminal.wires.length ; j++) {
         this.outputTerminal.wires[j].redraw();
      }
   },
   
   /**
    * Extend the getConfig to add the "codeText" property
    * @method getConfig
    */
   getConfig: function() {
      var obj = jsBox.Container.superclass.getConfig.call(this);
      obj.codeText = this.textarea.value;
      return obj;
   }
   
});


/**
 * Container class that contains a definition of a Taverna workflow.
 */
TavernaWFContainer = function(options, layer) {
TavernaWFContainer.superclass.constructor.call(this, options, layer);
    //this.wfInputPorts = [];
    //this.wfOutputPorts = [];
    this.wfURI = null;
};
YAHOO.lang.extend(TavernaWFContainer, WireIt.InOutContainer, {

    setOptions: function(options) {
        TavernaWFContainer.superclass.setOptions.call(this, options);
        this.options.xtype = "TavernaWFContainer";
    }
});

