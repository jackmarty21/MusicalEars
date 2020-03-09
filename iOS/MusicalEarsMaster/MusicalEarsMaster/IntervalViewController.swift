//
//  ViewController.swift
//  IntervalDetectorPrototype
//
//  Created by Jack Marty on 12/6/19.
//  Copyright © 2019 Jack Marty. All rights reserved.
//

import UIKit
import AudioKit
import AVFoundation

class IntervalViewController: UIViewController {

    @IBOutlet weak var micButton: UIButton!
    @IBOutlet weak var scoreLabel: UILabel!
    @IBOutlet weak var timerLabel: UILabel!
    @IBOutlet weak var intervalTimerLabel: UILabel!
    @IBOutlet weak var animationViewTop: UIView!
    @IBOutlet weak var animationViewBottom: UIView!
    
    let BASEAMPLITUDE = 0.1
    
    var AnimationControllerTop : ImageAnimation!
    var AnimationControllerBottom : ImageAnimation!
    var myColors = Colors()
    var baseSeconds = 1
    var intervalSeconds = 3
    var baseTimerDidStart = false
    var intervalTimerDidStart = false
    var scoreCtr = 0
    var isIntervalTest = false
    var micIsOn = false
    
    var myNotes = Notes()
    var playSoundFiles = PlaySound()
    var processTone = ProcessTone()
    var baseTimer = Timer()
    var intervalTimer = Timer()
    
    var mic: AKMicrophone!
    var tracker: AKFrequencyTracker!
    var silence: AKBooster!
    
    var randomNote = Int.random(in: 0..<12)
    var randomInterval = Int.random(in: 1..<12)
    var randomUpDown = Int.random(in: 0..<2)
    
    //Screen Size
    var labelWidth = CGFloat()
    var heightTop = CGFloat()
    var widthTop = CGFloat()
    var heightBottom = CGFloat()
    var widthBottom = CGFloat()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        
        animationViewTop.layer.borderWidth = 3
        animationViewTop.layer.cornerRadius = 10
        animationViewTop.layer.borderColor = myColors.primaryDarkBorder.cgColor
        animationViewBottom.layer.borderWidth = 3
        animationViewBottom.layer.cornerRadius = 10
        animationViewBottom.layer.borderColor = myColors.primaryDarkBorder.cgColor
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
        
        heightTop = self.animationViewTop.frame.size.height
        widthTop = self.animationViewTop.frame.size.width
        heightBottom = self.animationViewBottom.frame.size.height
        widthBottom = self.animationViewBottom.frame.size.width
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "ToAnimationTop" {
            AnimationControllerTop = (segue.destination as! ImageAnimation)
        }
        if segue.identifier == "ToAnimationBottom" {
            AnimationControllerBottom = (segue.destination as! ImageAnimation)
        }
    }
    
    @IBAction func playAudio(_ sender: Any) {
        playSoundFiles.playSound(fileName: String(randomNote))
    }
    
    @IBAction func nextNote(_ sender: Any) {
        randomNote = Int.random(in: 0..<12)
        randomInterval = Int.random(in: 1..<12)
        randomUpDown = Int.random(in: 0..<2)
        AnimationControllerTop.targetNote.text = myNotes.Notes[randomInterval].name
        AnimationControllerBottom.targetNote.text = myNotes.Notes[randomNote].name
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
        
        let baseTargetArray = myNotes.shiftArray(randomNote: randomNote)
        let intervalTargetArray = myNotes.shiftArray(randomNote: randomNote+randomInterval)
        if AnimationControllerTop != nil {
            AnimationControllerTop.height = heightTop
            AnimationControllerTop.labelWidth = widthTop-83
            AnimationControllerTop.targetNote.text = myNotes.Notes[randomInterval].name
        }
        if AnimationControllerBottom != nil {
            AnimationControllerBottom.height = heightBottom
            AnimationControllerBottom.labelWidth = widthBottom-83
            AnimationControllerBottom.targetNote.text = myNotes.Notes[randomNote].name
        }
        
        //If micophone heads a volume above this amplitude, continue
        if tracker.amplitude > BASEAMPLITUDE {
            
            let frequency : Float
            let roundedFrequency : Float?
            let index : Int
            let centAmountInt : Int
            let y : Int
            
            if isIntervalTest == true {
                //Set measured values from ProcessTone class
                frequency = processTone.getBaseFrequency(frequency: Float(tracker.frequency), targetArray: intervalTargetArray)
                roundedFrequency = Float(String(format: "%.3f", frequency))
                index = processTone.getMeasuredFreqIndex(targetArray: intervalTargetArray, roundedFrequency: roundedFrequency)
                centAmountInt = processTone.getCents(roundedFrequency: roundedFrequency, targetArray: intervalTargetArray)
                y = processTone.getYCoordinate(centAmountInt: centAmountInt, decrement: Int(heightTop/2-40))
                
                AnimationControllerTop.animateImage(y: y, centAmountInt: centAmountInt, amplitude: tracker.amplitude, baseAmplitude: BASEAMPLITUDE, duration: 3.3)
                
                //Start or Stop timer based off of cent value
                if abs(centAmountInt) <= 50 && intervalTimerDidStart == false {
                    startIntervalTimer()
                    intervalTimerDidStart = true
                    
                } else if abs(centAmountInt) > 50 {
                    intervalTimer.invalidate()
                    intervalTimerDidStart = false
                }
                //Find octave of measured note
                let octave = Int(log2f(Float(tracker.frequency) / frequency))
                AnimationControllerTop.noteLabel.text = "\(intervalTargetArray[index].name)\(octave)"
            } else {
                //Set measured values from ProcessTone class
                frequency = processTone.getBaseFrequency(frequency: Float(tracker.frequency), targetArray: baseTargetArray)
                roundedFrequency = Float(String(format: "%.3f", frequency))
               index = processTone.getMeasuredFreqIndex(targetArray: baseTargetArray, roundedFrequency: roundedFrequency)
                centAmountInt = processTone.getCents(roundedFrequency: roundedFrequency, targetArray: baseTargetArray)
                y = processTone.getYCoordinate(centAmountInt: centAmountInt, decrement: Int(heightBottom/2-40))
                
                AnimationControllerBottom.animateImage(y: y, centAmountInt: centAmountInt, amplitude: tracker.amplitude, baseAmplitude: BASEAMPLITUDE, duration: 1.3)
                
                //Start or Stop timer based off of cent value
                if abs(centAmountInt) <= 50 && baseTimerDidStart == false {
                    startBaseTimer()
                    baseTimerDidStart = true
                    
                } else if abs(centAmountInt) > 50 {
                    baseTimer.invalidate()
                    baseSeconds = 1
                    baseTimerDidStart = false
                }
                //Find octave of measured note
                let octave = Int(log2f(Float(tracker.frequency) / frequency))
                AnimationControllerBottom.noteLabel.text = "\(baseTargetArray[index].name)\(octave)"
            }
        } else {
            baseTimer.invalidate()
            baseSeconds = 1
            baseTimerDidStart = false
        }
        
    }
    
    //https://medium.com/ios-os-x-development/build-an-stopwatch-with-swift-3-0-c7040818a10f
    
    @objc func updateBaseTimer() {
        //user got base note correct
        if baseSeconds == 0 && isIntervalTest == false {
            baseTimer.invalidate()
            baseSeconds = 10
            baseTimerDidStart = false
            isIntervalTest = true
            startBaseTimer()
        }
        //User did not get target note, restart
        else if baseSeconds == 0 && isIntervalTest == true {
            baseTimer.invalidate()
            intervalTimer.invalidate()
            baseSeconds = 1
            intervalSeconds = 3
            baseTimerDidStart = false
            intervalTimerDidStart = false
            isIntervalTest = false
        }
        print("baseSeconds = \(baseSeconds)")
        print("intervalSeconds = \(intervalSeconds)")
        baseSeconds -= 1     //This will decrement(count down)the seconds.
        
    }
    @objc func updateIntervalTimer() {
        //user did get target note correct
        if intervalSeconds == 0 {
            baseTimer.invalidate()
            intervalTimer.invalidate()
            baseSeconds = 1
            intervalSeconds = 3
            baseTimerDidStart = false
            intervalTimerDidStart = false
            isIntervalTest = false
            randomNote = Int.random(in: 0..<12)
            randomInterval = Int.random(in: 1..<12)
            AnimationControllerTop.targetNote.text = myNotes.Notes[randomNote].name
            scoreCtr += 1
            scoreLabel.text = "\(scoreCtr) pts"
        }
        intervalSeconds -= 1     //This will decrement(count down)the seconds.
    }
    func startBaseTimer() {
        baseTimer = Timer.scheduledTimer(timeInterval: 1, target: self,   selector: (#selector(IntervalViewController.updateBaseTimer)), userInfo: nil, repeats: true)
    }
    func startIntervalTimer() {
        intervalTimer = Timer.scheduledTimer(timeInterval: 1, target: self,   selector: (#selector(IntervalViewController.updateIntervalTimer)), userInfo: nil, repeats: true)
    }
}

