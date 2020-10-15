BufAnalyzer {

	*initClass {

		Class.initClassTree(SynthDescLib);

		SynthDef(\bufAnalyzeOnsets, { arg buf, thresh=0.12, speedup=1, spacing= 0.2, monitorAmp = 0.1, out = 0;
			var name = '/bufAnalyzeOnsets';
			var buffr = BufFrames.ir(buf);
			var phase = Line.ar(0, buffr, BufDur.ir(buf) / speedup, doneAction: 2);
			var sig = BufRd.ar(1, buf, phase, 0, 1);
			//			var detect = PV_JensenAndersen.ar(
			var detect = Onsets.kr(
				FFT(LocalBuf(2048), sig),
				threshold: thresh
			);
			detect = Trig1.ar(detect, spacing);
			SendReply.ar(detect, '/bufAnalyzeOnsets', [buf, phase, Amplitude.kr(sig)]);
			// Silent.ar;
			Out.ar(out, sig * monitorAmp);
		}).add;
	}

	// analyzed one Buffer. Buffer obj as Argument;
	// returns peaks as Frames - secs would be much better to handle later,
	// but impreciser, but never mind - ms is enough.
	// returns List with frames for direct use and int types

	*analyzeOnsets  { arg buf, speedup=1, onDone, writeFile = true, postAll = false;
		var bufnum = buf.bufnum;
		var server = buf.server;
		var onsetsList = List[];
		var stopTime = buf.numFrames / server.sampleRate / speedup + (server.latency ? 0.2 * 2);

		var resp;
		"*** analyzing onsets for Buffer nr. % for % seconds.\n"
		.postf(bufnum, stopTime);

		// start OSCresp
		resp = OSCFunc({ |msg|
			var onsetFrame, onsetTime;
			// msg.postln;
			if (msg[3].asInteger == bufnum){
				onsetFrame = msg[4];
				// onsetTime = (onsetFrame / server.sampleRate).round(0.00001);
				onsetsList.add(onsetFrame);
				if (onsetsList.size % 20 == 0) {
					"% has % onsets so far ...\n"
					.postf(buf.path.basename, onsetsList.size);
				};
				// onsetTime.postln;
			}
		}, \bufAnalyzeOnsets).add;


		// start analyz synth
		( instrument: \bufAnalyzeOnsets, server: server, buf: bufnum ).play;

		// cleanup
		{ 	resp.remove;
			"// Found % attacks in Buffer nr. %: %\n".postf(onsetsList.size, bufnum);
			if (postAll) {
				onsetsList.asArray.postcs;
				"\n\n".postln;
			};
			if (writeFile) {
				if (buf.path.isNil) {
					"cannot write file ...".postln;
				} {
					File.use(buf.path.splitext[0] ++ ".onsets.txt", "w", { |f|
						f.write(onsetsList.asArray.cs);
						f.close;
					});
				}
			};

			onDone.value(onsetsList);

		}.defer(buf.numFrames / server.sampleRate / speedup + (server.latency ? 0.2 * 2));
	}
}
