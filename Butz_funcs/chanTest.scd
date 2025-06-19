(
Ndef(\chanTest).prime({ PinkNoise.ar(\amp.kr(0.25, 0.5)) });

Tdef(\chanTest).addSpec(\timeStep, [0.2, 5, \exp, 0.01]);
Tdef(\chanTest).addSpec(\numChans, [2, 100, \lin, 1]);

Tdef(\chanTest).set(\timeStep, 1);
Tdef(\chanTest).set(\numChans, s.options.numOutputBusChannels);

Tdef(\chanTest, { |env|
	var chan;
	loop {
		chan = Ndef(\chanTest).monitor.out ? 0;
		chan = chan + 1;
		if (chan >= env.numChans) {
			"chanTest begins".postln;
			chan = 0;
			Ndef(\chanTest).stop;
			env.timeStep.wait;
		};
		Ndef(\chanTest).play(chan.postln);

		env.timeStep.wait;
	};
});

WinBounds.addMake(\chanTest, {
	var w = Window("chanTest").front;
	w.addFlowLayout;
	NdefGui(Ndef(\chanTest), 4, w);
	TdefGui(Tdef(\chanTest), 4, w);
	w
});
Butz.add(\chanTest, { WinBounds.showOrMake(\chanTest) });
)