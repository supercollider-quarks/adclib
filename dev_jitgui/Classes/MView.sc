MView : JITView {

	*initClass {
		Class.initClassTree(this.superclass);
		styleDict.putAll((
			\indexCol: Color.grey(0.0, 0.5),
			\indexFont: Font("Monaco", 12),
			\round: 0.0001,
			\shiftMode: \stop
		));
	}

	init { |invalue, inparent, inbounds, inoptions|
		^super.init(invalue, inparent, inbounds, inoptions).mode_(\number);
	}

	makeDrawFunc {
		super.makeDrawFunc;
		drawFunc.add(\number, { this.drawNumber }, active: false);
		drawFunc.modes.put(\code, (on: \code, off: \number));
		drawFunc.modes.put(\number, (on: [\number, \code]));
	}

	doEnter { |uv, mod = 0|
		// overrides return in keyDownFuncs
		var newVal = try { dict[\editStr].interpret };
		if (newVal.notNil) {
			var spec = dict[\myspec];
			if (spec.notNil) {
				newVal = spec.constrain(newVal);
			} {
				"JITView - no spec for val, thus unconstrained: %\n"
				.postf(newVal);
			};

			this.valueAction_(newVal);
			dict.put(\editStr, nil);
		};
	}


	makeKeyDownActions {
		super.makeKeyDownActions;
		dict.put(\keyNumFuncs, (
			$x: { |uv, mod| this.shiftRange(-1.0, \stop).doAction },
			$X: { |uv, mod| this.setUni(this.makeVals(0.0)).doAction },

			$m: { |uv, mod| this.shiftRange(1.0, \stop).doAction },
			$M: { |uv, mod| this.setUni(this.makeVals(1.0)).doAction },

			$c: { |uv, mod|
				var currVals = this.getUni.asArray;
				var currCenter = mean([currVals.minItem, currVals.maxItem]);
				this.shiftRange(0.5 - currCenter, \stop).doAction },
			$C: { |uv, mod| this.setUni(this.makeVals(0.5)).doAction },

			$r: { |uv, mod|
				var currVals = this.getUni, jump;
				if (currVals.size < 1) {
					this.setUni(1.0.rand);
				} {
					jump = rrand(currVals.minItem.neg, 1-currVals.maxItem);
					this.shiftRange(jump, \stop)
				};
				this.doAction
			},
			$R: { |uv, mod|
				this.setUni(this.makeVals({ 1.0.rand; })).doAction
			}
		));

		// number mode with alt-n
		dict[\keyDownAltFuncs].put($n, { |uv, key, mod|
			this.mode_(\number.postcs) });

		dict[\keyNumFuncs].parent_(dict[\keyDownFuncs]);

		// simple 1, 2 or more number(s), like slider+numbox is now.
		uv.keyDownAction.add(\number, { |uv, key, mod|
			// [key, mod].postcs;
			uv.focus(true);

			if (mod.isAlt) {
				dict[\keyDownAltFuncs][key].value;
			} {

				if ("[]1234567890.e+-,".includes(key)) {
					dict.put(\editStr, dict[\editStr] ? "" ++ key);
				} {
					dict[\keyNumFuncs][key].value(uv, mod);
				};
			};
			uv.refresh;

		}, active: false);

		uv.keyDownAction.modes.put(\number, (on: \number, off: \code));
	}

	setUni { |normVal|
		var spec;
		if (normVal.isNil) { ^this };
		spec = dict[\myspec].value.asSpec;
		if (spec.notNil) { this.value_(spec.map(normVal)) };
	}

	getUni {
		var spec = dict[\myspec].value.asSpec;

		^if (spec.notNil and: { this.checkNumber(value) }) { (spec.unmap(value)) };
	}

	drawNumber {

		var xvals, valsRect, dotStep, dots;
		var leftEnd, rightEnd, rangeWidth, height;

		if (this.checkNumber) {
			// "JITView cannot display as number(s): ".post; value.postln;
			^this
		};

		try { xvals = this.getUni.asArray * dict[\width]; };
		if (xvals.isNil or: { xvals.isEmpty }) {
			// "JITView - cannot display as numbers:".post; value.postln;
			^this
		};

		leftEnd = xvals.minItem;
		rightEnd = xvals.maxItem;
		rangeWidth = rightEnd - leftEnd;
		height = dict[\height];

		valsRect = Rect(leftEnd, 2, rangeWidth, height - 4);

		dotStep = valsRect.height / (xvals.size);
		dots = xvals.collect { |val, i| val @ (i + 0.5 * dotStep + 2) };

		dict.put(\xvals, xvals);
		dict.put(\dots, dots);

		// paint range area
		Pen.color_(dict[\knobCol]);
		Pen.fillRect(valsRect);

		Pen.color_(dict[\knobCol]);
		[leftEnd, rightEnd].do { |xpos|
			Pen.addRoundedRect(
				Rect(xpos-8, 2, 16, height - 4),
				5, 5
			);
		};
		Pen.fill;

		dots.do { |dot, i|
			if (i.even) {
				Pen.fillRect(
					Rect(leftEnd, dotStep * i + 2, rangeWidth, dotStep)
				);
			};
		};

		// paint dots
		Pen.color_(dict[\knobCCol]);
		Pen.width_(2);

		// depending on movemode, hilite
		// dot, bar, or whole knob

		dots.do { |dot, i|
			var nextdot = dots[i+1];
			Pen.addArc(dot, 6, 0, 2pi).fill;
			if (dots.size > 1) {
				Pen.stringAtPoint(i.asString, dot,
					dict[\indexFont], dict[\indexCol]);
			};
			// lines are disabled for now
			// if (nextdot.notNil) {
			// 	Pen.line(dot, nextdot).stroke;
			// };
		};
	}

	mouseDownNumber { |uv, x, y|
		var xy = x@y;
		var foundIndex;

		if (this.checkNumber(value).not) {
			"MView: value not number(s): %.\n".postf(value);
			^this
		};

		dict[\mousexy] = xy;
		dict[\normx] = x / dict[\width];
		dict[\moveMode] = \noMove;

		if (value.isKindOf(SimpleNumber)) {
			dict[\moveMode] = \number;
		} {
			foundIndex = dict[\dots].detectIndex { |dot|
				dot.dist(xy) < 6 };
			dict[\moveMode] = \shiftRange; // default
			dict[\foundIndex] = foundIndex; // index or nil

			// find selected dot:
			if (foundIndex.notNil) {
				//	"foundIndex: %\n".postf(foundIndex);
				dict[\moveMode] = \single;
			} {
				// no dot index found, so try borders
				if (x.absdif(dict[\xvals].minItem) < 8) {
					dict[\moveMode] = \scaleMin;
				} {
					if (x.absdif(dict[\xvals].maxItem) < 8) {
						dict[\moveMode] = \scaleMax;
					} {
						if (x.inclusivelyBetween(
							dict[\xvals].minItem,
							dict[\xvals].maxItem)) {
							dict[\moveMode] = \shiftRange;
		}; }; }; } };
	}

	mouseMoveNumber { |uv, x, y, mod|
			var normX = (x / dict[\width]);
			var xy = x@y;
			(
				\number: { this.setUni(normX).doAction },
				\shiftRange: {
					this.shiftRange(normX - dict[\normx], dict[\shiftMode]);
				},
				\scaleMin: { this.scaleMin(normX) },
				\scaleMax: { this.scaleMax(normX) },
				\single: {
					this.setNormNumByIndex(dict[\foundIndex], normX);
				}
			)[dict[\moveMode]].value;
			// cache these after doing the actions:
			dict[\mousexy] = xy;
			dict[\normx] = normX;
			this.doAction;
			this.refresh;
	}

	// methods when value is an array of numbers
	scaleMin { |normVal|
		var currVals = this.getUni;
		var currmin = currVals.minItem;
		var currmax = currVals.maxItem;
		this.setUni(currVals.linlin(
			currmin, currmax, normVal, currmax));
	}

	scaleMax { |normVal|
		var currVals = this.getUni;
		var currmin = currVals.minItem;
		var currmax = currVals.maxItem;
		this.setUni(currVals.linlin(
			currmin, currmax, currmin, normVal));
	}

	shiftRange { |normDiff = 0, mode = \stop|
		// modes can be \stop or \clip
		var currVals = this.getUni;
		if (currVals.size < 1) {
			^this.setUni(currVals + normDiff)
		};

		if (mode == \stop) {
			if (normDiff > 0) {
				normDiff = min(normDiff, 1 - currVals.maxItem);
			} {
				normDiff = max(normDiff, currVals.minItem.neg);
			};
		};
		this.setUni(this.getUni + normDiff)
	}

	setNormNumByIndex { |index, normval|
		value.put(index, dict[\myspec].map(normval));
	}


	makeMouseActions {
		super.makeMouseActions;

		uv.mouseDownAction.modes.put(\number, (on: \number, off: \code));
		uv.mouseMoveAction.modes.put(\number, (on: \number, off: \code));
		uv.mouseUpAction.modes.put(\number, (on: \number, off: \code));

		uv.mouseDownAction.modes.put(\code, (on: \code, off: \number));
		uv.mouseMoveAction.modes.put(\code, (on: \code, off: \number));
		uv.mouseUpAction.modes.put(\code, (on: \code, off: \number));

		uv.mouseDownAction.add(\number, { |uv, x, y, mod|
			this.mouseDownNumber(uv, x, y, mod);
		}, active: false);

		uv.mouseMoveAction.add(\number, { |uv, x, y, mod|
		this.mouseMoveNumber(uv, x, y, mod);
	});
	}

	number { this.mode_(\number).refresh; }
}
