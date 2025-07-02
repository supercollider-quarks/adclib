/*
// added tabSize param to fit larger trees when needed

s.boot;
s.plotTree (tabSize: 20);
g = Group.new.isPlaying_(true);
10.do { |i| Ndef(i.asSymbol).parentGroup_(g).put(0, { Silent.ar }).play };

fork { 100.do { ().play; 0.3.rand.wait } };

renamed these to plotTree2 and plotTreeView2 for now,
so they do not overwrite newer implementation in main SC.
Need to look whether anything is useful in the overwrites here.

*/

+ Server {
	plotTree2 {|interval=0.5, tabSize = 25|
		var onClose, window = Window.new(name.asString + "Node Tree",
			Rect(128, 64, 400, 400),
			scroll:true
		).front;
		window.view.hasHorizontalScroller_(false).background_(Color.grey(0.9));
		onClose = this.plotTreeView2(interval, window.view, { defer {window.close}; }, tabSize);
		window.onClose = {
			onClose.value;
		};
	}

	plotTreeView2 {|interval=0.5, parent, actionIfFail, tabSize = 25|
		var resp, done = false;
		var collectChildren, levels, countSize;
		var view, bounds;
		var updater, updateFunc;
		var pen, font;

		pen = GUI.current.pen;
		font = Font.sansSerif(10);

		view = UserView.new(parent, Rect(0,0,400,400));

		view.drawFunc = {
			var xtabs = 0, ytabs = 0, drawFunc;

			drawFunc = {|group|
				var thisSize, rect, endYTabs;
				xtabs = xtabs + 1;
				ytabs = ytabs + 1;
				pen.font = font;
				group.do({|node|
					if(node.value.isArray, {
						thisSize = countSize.value(node);
						endYTabs = ytabs + thisSize + 0.2;
						rect = Rect(xtabs * tabSize + 0.5,
							ytabs * tabSize + 0.5,
							parent.bounds.width - (xtabs * tabSize * 2),
							thisSize * tabSize;
						);
						pen.fillColor = Color.grey(0.8);
						pen.fillRect(rect);
						pen.strokeRect(rect);
						pen.color = Color.black;
						pen.stringInRect(
							" Group" + node.key.asString +
							(node.key == 1).if("- default group", ""),
							rect
						);
						drawFunc.value(node.value);
						ytabs = endYTabs;
					},{
						rect = Rect(xtabs * tabSize + 0.5,
							ytabs * tabSize + 0.5,
							7 * tabSize,
							0.8 * tabSize
						);
						pen.fillColor = Color.white;
						pen.fillRect(rect);
						pen.strokeRect(rect);
						pen.color = Color.black;
						pen.stringInRect(
							" " ++ node.key.asString + node.value.asString,
							rect
						);
						ytabs = ytabs + 1;
					});
				});
				xtabs = xtabs - 1;
			};
			drawFunc.value(levels);
		};

		// msg[1] controls included
		// msg[2] nodeID of queried group
		// initial number of children
		resp = OSCFunc({ arg msg;
			var finalEvent;
			var i = 2, j, controls, printControls = false, dumpFunc;
			if(msg[1] != 0, {printControls = true});
			dumpFunc = {|numChildren|
				var event, children;
				event = ().group;
				event.id = msg[i];
				event.instrument = nil; // need to know it's a group
				i = i + 2;
				children = Array.fill(numChildren, {
					var id, child;
					// i = id
					// i + 1 = numChildren
					// i + 2 = def (if synth)
					id = msg[i];
					if(msg[i+1] >=0, {
						child = dumpFunc.value(msg[i+1]);
					}, {
						j = 4;
						child = ().synth.instrument_(msg[i+2]);
						if(printControls, {
							controls = ();
							msg[i+3].do({
								controls[msg[i + j]] = msg[i + j + 1];
								j = j + 2;
							});
							child.controls = controls;
							i = i + 4 + (2 * controls.size);
						}, {i = i + 3 });
					});
					child.id = id;
				});
				event.children = children;
				event;
			};
			finalEvent = dumpFunc.value(msg[3]);
			done = true;
			collectChildren = {|group|
				group.children.collect({|child|
					if(child.children.notNil,{
						child.id -> collectChildren.value(child);
					}, {
						child.id -> child.instrument;
					});
				});
			};
			levels = collectChildren.value(finalEvent);
			countSize = {|array|
				var size = 0;
				array.do({|elem|
					if(elem.value.isArray, { size = size + countSize.value(elem.value) + 2}, {size = size + 1;});
				});
				size
			};
			defer {
				view.bounds = Rect(0, 0, 400, max(400, tabSize * (countSize.value(levels) + 2)));
				view.refresh;
			}
		}, '/g_queryTree.reply', addr).fix;

		updateFunc = {
			updater = fork {
				loop {
					this.sendMsg("/g_queryTree", 0, 0);
					interval.wait;
				}
			}
		};
		updateFunc.value;
		CmdPeriod.add(updateFunc);
		SystemClock.sched(3, {
			if(done.not, {
				actionIfFail.value();
				"Server failed to respond to Group:queryTree!".warn;
			});
		});
		//action to be executed when enclosing window closes
		^{
			updater.stop;
			CmdPeriod.remove(updateFunc);
			resp.free;
		}
	}
}