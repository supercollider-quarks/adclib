Butz.add(\mixer, { WinBounds.showOrMake(\mixer) });
WinBounds.addMake(\mixer, {
	var m = NdefMixer(s);
	ProxyMeter.addMixer(m);
	m.parent.name_(\mixer)
});
