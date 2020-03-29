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

    @IBOutlet weak var timerLabel: UILabel!
    @IBOutlet weak var scoreLabel: UILabel!
    @IBOutlet weak var micButton: UIButton!
    @IBOutlet weak var animationView: UIView!
    
    let BASEAMPLITUDE = 0.03
    
    var AnimationController : ImageAnimation!
    var seconds = 3
    var timer = Timer()
    var countingTimer = Timer()
    var timerDidStart = false
    var countingTimerStarted = false
    var scoreCtr = 0
    var countingTimerSeconds = 0
    
    var myColors = Colors()
    var myNotes = Notes()
    var playSoundFiles = PlaySound()
    var processTone = ProcessTone()
    var micIsOn = false
    
    var mic: AKMicrophone!
    var tracker: AKFrequencyTracker!
    var silence: AKBooster!
    
    var randomNote = Int.random(in: 0..<23)
    
    //Screen Size
    var labelWidth = CGFloat()
    var height = CGFloat()
    var width = CGFloat()
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        
        animationView.layer.borderWidth = 3
        animationView.layer.cornerRadius = 10
        animationView.layer.borderColor = myColors.primaryDarkBorder.cgColor
        scoreLabel.textColor = myColors.primaryDarkText
        timerLabel.textColor = myColors.primaryDarkText
        
                
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
        
    }

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)

        height = self.animationView.frame.size.height
        width = self.animationView.frame.size.width
        
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
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "ToAnimation" {
            AnimationController = (segue.destination as! ImageAnimation)
        }
    }
    
    @IBAction func nextNote(_ sender: Any) {
        randomNote = Int.random(in: 0..<12)
        AnimationController.targetNote.text = myNotes.Notes[(randomNote+8)%12].name
    }
    
    @IBAction func playAudio(_ sender: Any) {
        if countingTimerStarted == false {
            countingTimerStarted = true
            startCountingTimer()
        }
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
        let targetArray = myNotes.shiftArray(randomNote: (randomNote+8)%12)
        
        if AnimationController != nil {
            AnimationController.height = height
            AnimationController.labelWidth = width-83
            AnimationController.targetNote.text = myNotes.Notes[(randomNote+8)%12].name
        }
        //Set measured values from ProcessTone class
        let frequency = processTone.getBaseFrequency(frequency: Float(tracker.frequency), targetArray: targetArray)
        let roundedFrequency = Float(String(format: "%.3f", frequency))
        let index = processTone.getMeasuredFreqIndex(targetArray: targetArray, roundedFrequency: roundedFrequency)
        let centAmountInt = processTone.getCents(roundedFrequency: roundedFrequency, targetArray: targetArray)
        let y = processTone.getYCoordinate(centAmountInt: centAmountInt, decrement: (Int(height)/2)-40)
        
        AnimationController.animateImage(y: y, centAmountInt: centAmountInt, amplitude: tracker.amplitude, baseAmplitude: BASEAMPLITUDE, duration: 3.3)
        
        //If micophone heads a volume above this amplitude, continue
        if tracker.amplitude > BASEAMPLITUDE {
            //Start or Stop timer based off of cent value
            if abs(centAmountInt) <= 50 && timerDidStart == false {
                startTimer()
                timerDidStart = true
                
            } else if abs(centAmountInt) > 50 {
                timer.invalidate()
                seconds = 3
                timerDidStart = false
            }
            
            //Find octave of measured note
            let octave = Int(log2f(Float(tracker.frequency) / frequency))
            AnimationController.noteLabel.text = "\(targetArray[index].name)\(octave)"
        } else {
            timer.invalidate()
            seconds = 3
            timerDidStart = false
        }
        
    }
    
    //https://medium.com/ios-os-x-development/build-an-stopwatch-with-swift-3-0-c7040818a10f
    
    @objc func updateTimer() {
        if seconds == 0 {
            playSoundFiles.playSound(fileName: "sucess")
            AnimationController.animateImageOff()
            timer.invalidate()
            seconds = 3
            timerDidStart = false
            randomNote = Int.random(in: 0..<12)
            AnimationController.targetNote.text = myNotes.Notes[(randomNote+8)%12].name
            scoreCtr += 1
            scoreLabel.text = "\(scoreCtr) pts"
        }
        seconds -= 1     //This will decrement(count down)the seconds.
        
    }
    func startTimer() {
        timer = Timer.scheduledTimer(timeInterval: 1, target: self,   selector: (#selector(PitchViewController.updateTimer)), userInfo: nil, repeats: true)
    }
    @objc func updateCountingTimer() {
        countingTimerSeconds += 1
        let minutes = Int(countingTimerSeconds) / 60 % 60
        let seconds = Int(countingTimerSeconds) % 60
        timerLabel.text = String(format:"%02i:%02i", minutes, seconds)
    }
    func startCountingTimer() {
        countingTimer = Timer.scheduledTimer(timeInterval: 1, target: self,   selector: (#selector(PitchViewController.updateCountingTimer)), userInfo: nil, repeats: true)
    }
}

