
#define NO_NOTE 0

//These notes are in Hertz.
double NOTE_C0 = 16.3516d;
double NOTE_C0s = 17.3239d;
double NOTE_D0 = 18.354d;
double NOTE_D0s = 19.4454d;
double NOTE_E0 = 20.6017d;
double NOTE_F0 = 21.8268d;
double NOTE_F0s = 23.1247d;
double NOTE_G0 = 24.4997d;
double NOTE_G0s = 25.9565d;
double NOTE_A0 = 27.5d;
double NOTE_A0s = 29.1352d;
double NOTE_B0 = 30.8677d;
double NOTE_C1 = 32.7032d;
double NOTE_C1s = 34.6478d;
double NOTE_D1 = 36.7081d;
double NOTE_D1s = 38.8909d;
double NOTE_E1 = 41.2034d;
double NOTE_F1 = 43.6535d;
double NOTE_F1s = 46.2493d;
double NOTE_G1 = 48.9994d;
double NOTE_G1s = 51.9131d;
double NOTE_A1 = 55.0d;
double NOTE_A1s = 58.2705d;
double NOTE_B1 = 61.7354d;
double NOTE_C2 = 65.4064d;
double NOTE_C2s = 69.2957d;
double NOTE_D2 = 73.4162d;
double NOTE_D2s = 77.7817d;
double NOTE_E2 = 82.4069d;
double NOTE_F2 = 87.3071d;
double NOTE_F2s = 92.4986d;
double NOTE_G2 = 97.9989d;
double NOTE_G2s = 103.826d;
double NOTE_A2 = 110.0d;
double NOTE_A2s = 116.541d;
double NOTE_B2 = 123.471d;
double NOTE_C3 = 130.813d;
double NOTE_C3s = 138.591d;
double NOTE_D3 = 146.832d;
double NOTE_D3s = 155.563d;
double NOTE_E3 = 164.814d;
double NOTE_F3 = 174.614d;
double NOTE_F3s = 184.997d;
double NOTE_G3 = 195.998d;
double NOTE_G3s = 207.652d;
double NOTE_A3 = 220.0d;
double NOTE_A3s = 233.082d;
double NOTE_B3 = 246.942d;
double NOTE_C4 = 261.626d;
double NOTE_C4s = 277.183d;
double NOTE_D4 = 293.665d;
double NOTE_D4s = 311.127d;
double NOTE_E4 = 329.628d;
double NOTE_F4 = 349.228d;
double NOTE_F4s = 369.994d;
double NOTE_G4 = 391.995d;
double NOTE_G4s = 415.305d;
double NOTE_A4 = 440.0d;
double NOTE_A4s = 466.164d;
double NOTE_B4 = 493.883d;
double NOTE_C5 = 523.251d;
double NOTE_C5s = 554.365d;
double NOTE_D5 = 587.33d;
double NOTE_D5s = 622.254d;
double NOTE_E5 = 659.255d;
double NOTE_F5 = 698.456d;
double NOTE_F5s = 739.989d;
double NOTE_G5 = 783.991d;
double NOTE_G5s = 830.609d;
double NOTE_A5 = 880.0d;
double NOTE_A5s = 932.328d;
double NOTE_B5 = 987.767d;
double NOTE_C6 = 1046.5d;

/*There are some errors up here too...
 *Just disable the octave (with the exception of C6)
double NOTE_C6s = 1108.73d;
double NOTE_D6 = 1174.66d;
double NOTE_D6s = 1244.51d;
double NOTE_E6 = 1318.51d; //This value causes the head to fail.
double NOTE_F6 = 1396.91d;
double NOTE_F6s = 1479.98d;
double NOTE_G6 = 1567.98d;
double NOTE_G6s = 1661.22d; //This value does not produce a distinguishable pitch.
double NOTE_A6 = 1760.0d;
double NOTE_A6s = 1864.66d;
double NOTE_B6 = 1975.53d;*/

//Notes beyond this point do not make good sounds on the floppies.
//Timing is incorrect for some.
/*double NOTE_C7 = 2093.0d;
double NOTE_C7s = 2217.46d;
double NOTE_D7 = 2349.32d;
double NOTE_D7s = 2489.02d;
double NOTE_E7 = 2637.02d;
double NOTE_F7 = 2793.83d;
double NOTE_F7s = 2959.96d;
double NOTE_G7 = 3135.96d;
double NOTE_G7s = 3322.44d;
double NOTE_A7 = 3520.0d;
double NOTE_A7s = 3729.31d;
double NOTE_B7 = 3951.07d;
double NOTE_C8 = 4186.01d;
double NOTE_C8s = 4434.92d;
double NOTE_D8 = 4698.64d;
double NOTE_D8s = 4978.03d;
double NOTE_E8 = 5274.04d;
double NOTE_F8 = 5587.65d;*/

//Absolutely no support for these notes.
/*double NOTE_F8s = NOTE_A4;
double NOTE_G8 = NOTE_A4;
double NOTE_G8s = NOTE_A4;
double NOTE_A8 = NOTE_A4;
double NOTE_A8s = NOTE_A4;
double NOTE_B8 = NOTE_A4;*/

int naturals[49] = 
{
    NOTE_A0, NOTE_B0, NOTE_C0, NOTE_D0, NOTE_E0, NOTE_F0, NOTE_G0,
    NOTE_A1, NOTE_B1, NOTE_C1, NOTE_D1, NOTE_E1, NOTE_F1, NOTE_G1,
    NOTE_A2, NOTE_B2, NOTE_C2, NOTE_D2, NOTE_E2, NOTE_F2, NOTE_G2,
    NOTE_A3, NOTE_B3, NOTE_C3, NOTE_D3, NOTE_E3, NOTE_F3, NOTE_G3,
    NOTE_A4, NOTE_B4, NOTE_C4, NOTE_D4, NOTE_E4, NOTE_F4, NOTE_G4,
	NOTE_A5, NOTE_B5, NOTE_C5, NOTE_D5, NOTE_E5, NOTE_F5, NOTE_G5,
	NOTE_A6, NOTE_B6, NOTE_C6, NOTE_D6, NOTE_E6, NOTE_F6, NOTE_G6
};

int sharps [35] =
{
    NOTE_A0s, NOTE_C0s, NOTE_D0s, NOTE_F0s, NOTE_G0s,
    NOTE_A1s, NOTE_C1s, NOTE_D1s, NOTE_F1s, NOTE_G1s,
    NOTE_A2s, NOTE_C2s, NOTE_D2s, NOTE_F2s, NOTE_G2s,
    NOTE_A3s, NOTE_C3s, NOTE_D3s, NOTE_F3s, NOTE_G3s,
    NOTE_A4s, NOTE_C4s, NOTE_D4s, NOTE_F4s, NOTE_G4s,
	NOTE_A5s, NOTE_C5s, NOTE_D5s, NOTE_F5s, NOTE_G5s,
	NOTE_A6s, NOTE_C6s, NOTE_D6s, NOTE_F6s, NOTE_G6s
};

#define ASCII_ZERO 48
#define ASCII_CAP_A 65

int getNote(int note, int sharp, int octave)
{
	//Do not use octaves >= 7.
	octave = octave % 7;
	
    if (note == 0) return NO_NOTE;
    if (sharp == 0)
    {
        return naturals[(note - 1) + octave * 7];
    }
    else //sharp == 1
    {
        switch (note)
        {
            case 1: //A#
                return sharps[0 + octave * 5];
            case 3: //C#
                return sharps[1 + octave * 5];
            case 4: //D#
                return sharps[2 + octave * 5];
            case 6: //F#
                return sharps[3 + octave * 5];
            case 7: //G#
                return sharps[4 + octave * 5];
            default:
                return NO_NOTE; //Don't support E# or B#.
        }
    }
}

void getNoteName(int note, int sharp, int octave, char* result)
{
	//Do not use octaves >= 7.
	octave = octave % 7;
	
	if (note == 0)
	{
		result = "No note.";
		return result;
	}
	
	int ptr = 0;
	result[0] = (note - 1) + ASCII_CAP_A;
	result[1] = octave + ASCII_ZERO;
	
	if (sharp == 1)
	{
		result[2] = '#';
		result[3] = '\0';
	}
	else
	{
		result[2] = '\0';
	}	
}