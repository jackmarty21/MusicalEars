//
//  ViewController.swift
//  PitchDetectorPrototype
//
//  Created by Jack Marty on 12/6/19.
//  Copyright © 2019 Jack Marty. All rights reserved.
//

import UIKit
import AudioKit

class PitchViewController: UIViewController {

    @IBOutlet weak var Note: UILabel!
    @IBOutlet weak var targetNote: UILabel!
    @IBOutlet weak var timerLabel: UILabel!
    @IBOutlet weak var scoreLabel: UILabel!
    @IBOutlet weak var noteImageOff: UIImageView!
    @IBOutlet weak var noteImageOn: UIImageView!
    @IBOutlet weak var micButton: UIButton!
    @IBOutlet weak var noteConstraint: NSLayoutConstraint!
    @IBOutlet weak var imageOnConstraint: NSLayoutConstraint!
    @IBOutlet weak var imageOffConstraint: NSLayoutConstraint!
    @IBOutlet weak var greenWidthConstraint: NSLayoutConstraint!
    @IBOutlet weak var blackWidthConstraint: NSLayoutConstraint!
    
    var seconds = 3
    var timer = Timer()
    var timerDidStart = false
    var scoreCtr = 0
    
    var myNotes = Notes()
    var playSoundFiles = PlaySound()
    var processTone = ProcessTone()
    var micIsOn = false
    
    var mic: AKMicrophone!
    var tracker: AKFrequencyTracker!
    var silence: AKBooster!
    
    var randomNote = Int.random(in: 0..<12)
    
    //Screen Size
    let screenWidth  = UIScreen.main.fixedCoordinateSpace.bounds.width
    let screenHeight = UIScreen.main.fixedCoordinateSpace.bounds.height
    var labelWidth = CGFloat()
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        
        labelWidth = screenWidth - 60
        blackWidthConstraint.constant = labelWidth

        
        do {
            try AudioKit.stop()
        } catch {
            print(error)
        }
        
        AKSettings.audioInputEnabled = true
        AKSettings.defaultToSpeaker = true
        AKSettings.useBluetooth = true
        
        mic = AKMicrophone()
        mic.stop()
        tracker = AKFrequencyTracker(mic)
        silence = AKBooster(tracker, gain: 0)
        
        targetNote.text = myNotes.Notes[randomNote].name
        Note.text = myNotes.Notes[randomNote].name
        Note.center = CGPoint(x: 40, y: screenHeight/2)
        noteImageOff.center = CGPoint(x: 40, y: screenHeight/2)
        noteImageOff.center = CGPoint(x: 40, y: screenHeight/2)
        
    }

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)

        AudioKit.output = silence
        do {
            try AudioKit.start()
        } catch {
            AKLog("AudioKit did not start!")
        }
        Timer.scheduledTimer(timeInterval: 0.05,
                             target: self,
                             selector: #selector(PitchViewController.updateUI),
                             userInfo: nil,
                             repeats: true)
    }
    
    @IBAction func playAudio(_ sender: Any) {
        if (micIsOn == true) {
            micIsOn = false
            mic.stop()
            micButton.setImage(UIImage(named: "mic_off.jpg"), for: .normal)
            playSoundFiles.playSound(fileName: String(randomNote))
            sleep(2)
            mic.start()
            micButton.setImage(UIImage(named: "mic_on.jpg"), for: .normal)
            micIsOn = true
        }
        else {
            playSoundFiles.playSound(fileName: String(randomNote))
        }
    }

    @IBAction func micButtonPressed(_ sender: Any) {
        if micIsOn == true {
            micIsOn = false
            micButton.setImage(UIImage(named: "mic_off.jpg"), for: .normal)
            mic.stop()
        }
        else {
            micIsOn = true
            micButton.setImage(UIImage(named: "mic_on.jpg"), for: .normal)
            mic.start()
        }
    }
    
    @objc func updateUI() {
        let targetArray = myNotes.shiftArray(randomNote: randomNote)
        let screenHeightInt = Int(screenHeight)
        
        //If micophone heads a volume above this amplitude, continue
        if tracker.amplitude > 0.03 {
            
            //Set measured values from ProcessTone class
            let frequency = processTone.getBaseFrequency(frequency: Float(tracker.frequency), targetArray: targetArray)
            let roundedFrequency = Float(String(format: "%.3f", frequency))
            let index = processTone.getMeasuredFreqIndex(targetArray: targetArray, roundedFrequency: roundedFrequency)
            let centAmountInt = processTone.getCents(roundedFrequency: roundedFrequency, targetArray: targetArray)
            let y = processTone.getYCoordinate(initialCoordinate: screenHeightInt/2, centAmountInt: centAmountInt, decrement: 200-40)
            
            imageOnConstraint.constant = CGFloat(y)
            imageOffConstraint.constant = CGFloat(y)
            noteConstraint.constant = CGFloat(y)
            
            UIView.animate(withDuration: 0.2, delay: 0.0, options: [],
            animations: {
                self.view.layoutIfNeeded()
            },
            completion: nil)
            
            //Start or Stop timer based off of cent value
            if abs(centAmountInt) <= 50 && timerDidStart == false {
                startTimer()
                timerDidStart = true
                greenWidthConstraint.constant = labelWidth
                //greenOffAnimate.stopAnimation(true)
                //greenOnAnimate.startAnimation()
                //self.view.layer.removeAllAnimations()
                UIView.animate(withDuration: 3.7, delay: 0.0, options: [.curveLinear],
                animations: {
                    self.noteImageOff.alpha = 0
                    NSLayoutConstraint.deactivate([self.greenWidthConstraint])
                    self.greenWidthConstraint.constant = self.labelWidth
                    NSLayoutConstraint.activate([self.greenWidthConstraint])
                    self.view.layoutIfNeeded()
                },
                completion: nil)
                
            } else if abs(centAmountInt) > 50 {
                timer.invalidate()
                seconds = 3
                timerLabel.text = "\(seconds)"
                timerDidStart = false
                UIView.animate(withDuration: .2, delay: 0.0, options: [.curveLinear],
                animations: {
                    self.noteImageOff.alpha = 1
                    NSLayoutConstraint.deactivate([self.greenWidthConstraint])
                    self.greenWidthConstraint.constant = 0
                    NSLayoutConstraint.activate([self.greenWidthConstraint])
                    self.view.layoutIfNeeded()
                },
                completion: nil)
            }
            
            //Find octave of measured note
            let octave = Int(log2f(Float(tracker.frequency) / frequency))
            Note.text = "\(targetArray[index].name)\(octave)"
        } else {
            timer.invalidate()
            seconds = 3
            timerLabel.text = "\(seconds)"
            timerDidStart = false

            UIView.animate(withDuration: 0.2, delay: 0.0, options: [.curveLinear],
            animations: {
                self.noteImageOff.alpha = 1
                NSLayoutConstraint.deactivate([self.greenWidthConstraint])
                self.greenWidthConstraint.constant = 0
                NSLayoutConstraint.activate([self.greenWidthConstraint])
                self.view.layoutIfNeeded()
            },
            completion: nil)
        }
        
    }
    
    //https://medium.com/ios-os-x-development/build-an-stopwatch-with-swift-3-0-c7040818a10f
    
    @objc func updateTimer() {
        if seconds == 0 {
//            greenOnAnimate.stopAnimation(true)
            //greenOffAnimate.startAnimation()
            //self.view.layer.removeAllAnimations()
            greenWidthConstraint.constant = 0
            timer.invalidate()
            seconds = 3
            timerLabel.text = "\(seconds)"
            timerDidStart = false
            randomNote = Int.random(in: 0..<12)
            targetNote.text = myNotes.Notes[randomNote].name
            scoreCtr += 1
            scoreLabel.text = "\(scoreCtr) pts"
        }
        seconds -= 1     //This will decrement(count down)the seconds.
        timerLabel.text = "\(seconds)" //This will update the label.
        
    }
    func startTimer() {
        timer = Timer.scheduledTimer(timeInterval: 1, target: self,   selector: (#selector(PitchViewController.updateTimer)), userInfo: nil, repeats: true)
    }
}

