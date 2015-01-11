package sound;

import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import support.FinePoint;
import ui.PortalListener;

public class SoundManager implements PortalListener {
	public enum SoundType {
		/* @formatter:off */
		stBirth("Birth.wav", 12), 
		stBeep("Beep.wav", 12), 
		stBigBlaster("BigBlaster.wav", 32), 
		stBigExplosion("BigExplosion.wav", 32), 
		stBlaster("Blaster.wav", 32), 
		stEndRound("EndRound.wav", 4), 
		stExplosion("Explosion.wav", 32), 
		stExtinction("Extinction.wav", 12), 
		stGrenade("Grenade.wav", 32), 
		stSmallExplosion("SmallExplosion.wav", 32), 
		stTinyExplosion("TinyExplosion.wav", 32);
		/* @formatter:on */

		public final String filename;
		AudioInputStream[] stream;
		Clip[] clip;
		public final int numClips;
		int nextClip = 0;
		public int numPlaying;

		SoundType(String _filename, int clips) {
			numClips = clips;
			clip = new Clip[numClips];
			stream = new AudioInputStream[numClips];
			filename = _filename;
			for (nextClip = 0; nextClip < numClips; nextClip++) {
				stream[nextClip] = makeStream();
				clip[nextClip] = makeClip();
			}
			nextClip = 0;
		}

		public Clip getClip() {
			return clip[nextClip];
		}

		public synchronized boolean open() {
			// Complete running sounds before starting new ones
			try {
				if (!clip[nextClip].isOpen()) {
					clip[nextClip].open(stream[nextClip]);
					clip[nextClip].setFramePosition(0);
					stream[nextClip].reset();
					return true;
				} else
					return false;
			} catch (LineUnavailableException | IOException e) {
				e.printStackTrace();
				return false;
			}
		}

		/**
		 * Play the sound from the beginning.
		 */
		public synchronized void play() {
			if (numPlaying >= numClips)
				return;
			clip[nextClip].start();
			numPlaying++;
			nextClip = (nextClip + 1) % numClips;
		}

		Clip makeClip() {
			Clip clip = null;
			if (stream[nextClip] == null)
				return null;
			try {
				AudioFormat format = stream[nextClip].getFormat();
				DataLine.Info info = new DataLine.Info(Clip.class, format);
				clip = (Clip) AudioSystem.getLine(info);
				clip.addLineListener(new LineListener() {
					@Override
					public void update(LineEvent evt) {
						if (evt.getType() == LineEvent.Type.STOP) {
							((Clip) evt.getSource()).close();
							numPlaying--;
						}
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (stream[nextClip] != null)
					try {
						stream[nextClip].close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
			return clip;
		}

		AudioInputStream makeStream() {
			AudioInputStream ais = null;
			AudioInputStream reusableAis = null;
			// Load each sound as a byte array. This is so the reset() command
			// will work, making
			// the sound reusable without reloading it.
			try {
				ais = AudioSystem.getAudioInputStream(getClass()
						.getResourceAsStream(filename));
				byte[] buffer = new byte[1024 * 32];
				int read = 0;
				ByteArrayOutputStream baos = new ByteArrayOutputStream(
						buffer.length);
				while ((read = ais.read(buffer, 0, buffer.length)) != -1) {
					baos.write(buffer, 0, read);
				}
				reusableAis = new AudioInputStream(new ByteArrayInputStream(
						baos.toByteArray()), ais.getFormat(),
						AudioSystem.NOT_SPECIFIED);
			} catch (UnsupportedAudioFileException | IOException e) {
				e.printStackTrace();
			} finally {
				if (ais != null) {
					try {
						ais.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return reusableAis;
		}
	}

	//Sound positioning
	static FinePoint viewpoint = new FinePoint();
	static Rectangle visibleWorld = new Rectangle();
	
	//Sound manipulation
	static final float kSoundDistanceFadeFactor = 1.2f;
	static final float kSoundDistancePanFactor = .1f;
	static final int kMaxHearingDistance = 40;
	static final float kMaxVolume = 6;
	static final float kMinVolume = -80;	
	static boolean muted = true;
	static SoundManager manager = new SoundManager();
	
	SoundManager() {
		
	}
	
	public static SoundManager getManager() {
		return manager;
	}

	public static void setMuted(boolean mute) {
		muted = mute;
	}

	public static boolean getMuted() {
		return muted;
	}

	public static void playSound(final SoundType sound) {
		if (muted)
			return;
		new Runnable() {
			@Override
			public void run() {
				if (sound.open())
					sound.play();
			}
		}.run();
	}

	/**
	 * Play a sound at a given location in the world. Sound balance and volume
	 * are affected by its distance and direction from the viewpoint
	 * 
	 * @param sound
	 * @param location
	 * @param viewpoint
	 */
	public static void playSound(final SoundType sound, FinePoint location) {
		if (muted)
			return;
		// Fade the sound based on how far it was from the viewpoint
		double distance = location.distance(viewpoint);
		if (distance < kMaxHearingDistance) {
			float temp = 0;
			if (!visibleWorld.contains(location))
				temp = kMinVolume;
			else
				temp = 7 - kSoundDistanceFadeFactor
						* (float) distance;
			final float volume = temp;
			final float balance = (float) (kSoundDistancePanFactor * (location.x - viewpoint.x));
			new Runnable() {
				@Override
				public void run() {
					if (sound.open()) {
						if (sound.getClip().isControlSupported(
								FloatControl.Type.MASTER_GAIN)) {
							FloatControl volumeControl = (FloatControl) sound
									.getClip().getControl(
											FloatControl.Type.MASTER_GAIN);
							volumeControl.setValue(Math.max(kMinVolume,
									Math.min(volume, kMaxVolume)));
						}
						if (sound.getClip().isControlSupported(
								FloatControl.Type.BALANCE)) {
							FloatControl balanceControl = (FloatControl) sound
									.getClip().getControl(
											FloatControl.Type.BALANCE);
							balanceControl.setValue(Math.max(-1,
									Math.min(balance, 1)));
						}
						sound.play();
					}
				}
			}.run();
		}
	}

	@Override
	public void setViewWindow(Object source, FinePoint p) {
		//Not used	
	}

	@Override
	public void setVisibleWorld(Object source, Rectangle r) {
		visibleWorld = r;	
		visibleWorld.grow((int)(r.width * 0.1), (int)(r.height * .1));
		viewpoint.x = r.getCenterX();
		viewpoint.y = r.getCenterY();
	}

	@Override
	public void addPortalListener(PortalListener pl) {
		//Not used
	}

	/*
	 * public static void main(String[] args) throws LineUnavailableException {
	 * SoundManager.setManager(new SoundManager()); JFrame frame = new JFrame();
	 * frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); JPanel pane = new
	 * JPanel(); pane.setLayout(new FlowLayout()); frame.setContentPane(pane);
	 * for (final SoundType type : SoundType.values()) { JButton button = new
	 * JButton(type.filename); button.addActionListener(new ActionListener() {
	 * int distance = 1;
	 * 
	 * @Override public void actionPerformed(ActionEvent arg0) { try {
	 * SoundManager.playSound(type, new FinePoint(distance, distance)); } catch
	 * (Exception e) { e.printStackTrace(); } distance = (distance + 1) % 40; }
	 * }); pane.add(button); } frame.pack(); frame.setVisible(true); }
	 */
}
