
Butz {
	classvar <all, <names, <curr, <>funcsDir;
	classvar <w, <butz, <pop, <style, <>numButz = 12;

	var <name, <actions;

	*initClass {
		style = (
			winCol: Color.red,
			name: "Butz",
			font: Font(Font.defaultSansFace, 16),
			fontCol: Color.white,
			butCol: Color.yellow(1.0, 0.3),
			winLoc: 5@50,
			winExtent: 110@40, // minimize
			margins: [0,0],
			spacing: 0
		);

		// funcsDir = (Quark("adclib").localPath +/+ "Butz_funcs");

		all = ();
		names = List[];
		Butz(\top);
		this.curr = \top;
	}

	*curr_ { |name|
		var found = all[name];
		if (found.notNil) {
			curr = found
		} {
			"*** Butz:curr no butz found for name %!\n".postf(name);
		};
		"// Butz.curr is %.\n".postf(curr);
		this.update;
	}

	*hasWin { ^w.notNil and: { w.isClosed.not } }

	*new { |name|
		var found = all[name];
		found !? { ^found };
		^this.basicNew(name)
	}

	*basicNew { |name|
		^super.newCopyArgs(name.asSymbol).init
	}

	load { |path|
		var res = path.loadPaths;
		if (res.isKindOf(Function) or: res.isKindOf(MFunc)) {
			this.add(path.basename)
		}
	}

	init {
		actions = NamedList();
		all.put(this.name, this);
		if (names.includes(name).not) { names.add(this.name) };
		if (name != \top) {
			actions.dict.parent = Butz(\top).actions.dict
		};
		this.class.update
	}

	storeArgs { ^[name] }
	printOn { |stream| ^this.storeOn(stream) }

	// convenience for small/big flip
	*miniMax {
		var numToShow;
		if (Butz.hasWin) {
			numToShow = if (Butz.butz[1].visible, 1, nil);
			Butz.showButs(numToShow);
		}
	}

	*addMiniMax { Butz(\top).addMiniMax }
	addMiniMax {
		actions.addFirst(\miniMax, { Butz.miniMax  });
		this.class.update
	}

	*closeAll {
		Window.allWindows.copy.do { |win| if (win != w) { win.close } }
	}

	*run { |name|  Butz(\top).run(name) }
	run { |name|  actions[name].value }

	*add { |name, action| Butz(\top).add(name, action) }
	*remove { |name| Butz(\top).remove(name) }
	*clear { Butz(\top).clear }

	add { |name, action|
		var hasAction, topAction;
		if (name.isNil) {
			"Butz.add: cannot add action without name, so ignored.\n".postln;
			^this
		};

		hasAction = actions.dict.keys.includes(name);
		topAction = actions.dict[name];

		case {
			action.isNil and: hasAction
		} {
			// "no action given, but has one -> keeping action".postln;
			^this
		} { action.notNil } {
			// "adding given action".postln;
			actions.add(name, action)
		} { topAction.notNil } {
			// "adding dynamic lookup to parent dict".postln;
			actions.add(name, { topAction.value })
		} {
			// "adding blank func to keep order of names".postln;
			actions.add(name, { })
		};

		this.class.update
	}

	remove { |name|
		actions.removeAt(name);
		this.class.update
	}

	clear {
		actions.clear;
		this.class.update
	}

	*show {
		if (w.notNil and: { w.isClosed.not }) {
			try { w.front };
			^this
		};
		this.makeWin;
	}

	*checkFontSize {
		// estimate layout height, and reduce font size if needed
		var maxbutheight = (Window.screenBounds.height - 24 / (curr.actions.size));
		var maxfontsize = (maxbutheight / 2 - style.spacing).asInteger;
		if (style.font.size > maxfontsize) {
			"Butz: reduce fontsize to %".postf(maxfontsize);
			style.font.size = maxfontsize;
			butz.do(_.font_(style.font))
		}
	}

	*makeWin {
		var style = Butz.style;
		var winLocX = style.winLoc.x;
		var winLocY = style.winLoc.y;
		var initRect = Window.flipY(Rect(winLocX, winLocY, style.winExtent.x, style.winExtent.y));
		var numB = max(Butz.numButz, curr.actions.size);

		w = Window(style.name, initRect);
		w.alwaysOnTop_(true).userCanClose_(false);
		w.background_(style.winCol).alpha_(style.alpha ? 1);

		w.layout = VLayout(
			pop = PopUpMenu().font_(style.font),
			*(butz = numB.collect {
				Button(w).states_([this.blankState])
				.font_(style.font)
			})
		);
		w.layout.margins_(style.margins);
		w.layout.spacing_(style.spacing);

		pop.action = { Butz.curr = pop.item };
		w.onClose = { w = nil };

		this.update.showButs;

		^w.front;
	}

	*showButs { |butsToShow, wait = 0.01|
		var bnds = Butz.w.bounds;
		// flipped, so this stores orig top
		var origBottom = bnds.bottom;
		butsToShow = butsToShow ? Butz.butz.size;
		fork ({
			Butz.butz.do { |but, i|
				but.visible_(i < butsToShow);
			};
			wait.wait;
			// make it small, so it gets minimal size
			// for its number of butsToShow:
			Butz.w.bounds_(
				bnds.extent_(Butz.style.winExtent)
			);
			// now it has its minimum size,
			// move it to its previous top,
			// and limit to screen height
			Butz.w.bounds_(
				WinBounds.limitRectToScreen(
					// flipped, so this restores orig top
					Butz.w.bounds.bottom_(origBottom)
				)
			)
		}, AppClock);
	}

	*blankState { ^[ " . . . ", style.fontCol, style.butCol ] }

	*setButton { |index|
		var but, name, actions, action;

		actions = curr.actions;

		but = butz[index];
		but ?? {
			"*** %: no button at index % for %!\n".postf(this, index, name.cs);
			^this
		};
		name = actions.names[index] ? " . . . ".asSymbol;
		action = actions[index];

		but.states.do { |state| state.put(0, name) };

		defer { but.states_(but.states) };
		but.action = action;
	}

	*update {
		var namesToShow = all.keys(Array).sort;
		namesToShow = names ++ (namesToShow.removeAll(names));

		if (Butz.hasWin) {
			this.checkFontSize;

			pop.items = namesToShow;
			pop.value = namesToShow.indexOf(curr.name);

			butz.do { |bt, i| this.setButton(i) }
		}
	}
}
