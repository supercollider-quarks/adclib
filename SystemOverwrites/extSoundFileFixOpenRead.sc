/*
// this old Photoshop file has a resource fork, which hangs libsndfile 1.0.28!
SoundFile.openRead("/Users/adc/Pictures/DragonFyrAll.psd");
-> Therefor, catch it here until a libsndfile update fixes this.

// TODO: should probably catch all files with resource fork? how?
*/

+ SoundFile {
	// temp fix for SoundFile bug: avoid trying to open .psd files
	openRead { arg pathName;
		var fileext;
		path = pathName ? path;
		Platform.case(
			\osx, {
				fileext = path.splitext.last;
				if (fileext.notNil and: { fileext.containsi("psd") }) {
					"*** evil PSD: %\n".postf(path.cs);
					^false
				}
			}
		);
		^this.prOpenRead(path);
	}
}
