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

    
    @IBOutlet weak var targetNote: UILabel!
    @IBOutlet weak var Note: UILabel!
    @IBOutlet weak var scoreLabel: UILabel!
    @IBOutlet weak var timerLabel: UILabel!
    @IBOutlet weak var blueCircle: UIView!
    @IBOutlet weak var greenCircle: UIView!
    
    
    var baseSeconds = 1
    var intervalSeconds = 3
    var timerBase = Timer()
    var timerInterval = Timer()
    var isTimerRunning = false
    var baseTimerDidStart = false
    var intervalTimerDidStart = false
    var passedBaseFrequencyTest = false
    var scoreCtr = 0
    
    var audioPlayer = AVAudioPlayer()

    
    var mic: AKMicrophone!
    var tracker: AKFrequencyTracker!
    var silence: AKBooster!
    
    var randomNote = Int.random(in: 0..<12)
    var randomInterval = Int.random(in: 1..<12)
    
    let noteFrequencies = [16.35, 17.32, 18.35, 19.45, 20.6, 21.83, 23.12, 24.5, 25.96, 27.5, 29.14, 30.87]
    let noteNamesWithSharps = ["C", "C♯", "D", "D♯", "E", "F", "F♯", "G", "G♯", "A", "A♯", "B"]
    let intervalNames = ["m2", "M2", "m3", "M3", "P4", "Tri", "P5", "m6", "M6", "m7", "M7"]
    
    
    //Screen Size
    let screenWidth  = UIScreen.main.fixedCoordinateSpace.bounds.width
    let screenHeight = UIScreen.main.fixedCoordinateSpace.bounds.height
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        do {
            try AudioKit.stop()
        } catch {
            print(error)
        }
        
        AKSettings.audioInputEnabled = true
        AKSettings.defaultToSpeaker = true
        AKSettings.useBluetooth = true
        
        mic = AKMicrophone()
        tracker = AKFrequencyTracker(mic)
        silence = AKBooster(tracker, gain: 0)
        
        targetNote.text = noteNamesWithSharps[randomNote]
        Note.text = noteNamesWithSharps[randomNote]
        Note.center = CGPoint(x: screenWidth/2, y: (1.8*screenHeight/2-40))
        blueCircle.center = CGPoint(x: screenWidth/2, y: (1.8*screenHeight/2-40))
        greenCircle.center = CGPoint(x: screenWidth/2, y: (1.8*screenHeight/2-40))
        
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
                             selector: #selector(IntervalViewController.updateUI),
                             userInfo: nil,
                             repeats: true)
    }
    
    @IBAction func playAudio(_ sender: Any) {
        playSound(fileName: String(randomNote))
    }


    @objc func updateUI() {
        
        //If micophone reads a volume above this amplitude, continue
        if tracker.amplitude > 0.08 {
            
            if passedBaseFrequencyTest == false {
                baseFrequencyTest()
            } else {
                intervalFrequencyTest()
            }
            
            
        } else {
            timerBase.invalidate()
            timerInterval.invalidate()
            baseSeconds = 1
            timerLabel.text = "\(baseSeconds)"
            baseTimerDidStart = false
            intervalTimerDidStart = false
            
            UIView.animate(withDuration: 0.01, delay: 0.0, options: [],
            animations: {
                self.blueCircle.alpha = 1
            },
            completion: nil)
        }
        
        //amplitude.text = String(format: "%0.2f", tracker.amplitude)
    }
    // MARK: - Base Frequency Test
    func baseFrequencyTest() {
        print("In baseFrequencyTest...")
        let targetArrayFrequencies = createNewDoubleArray(array: noteFrequencies, isTarget: false)
        let targetArrayNoteNames = createNewStringArray(array: noteNamesWithSharps, isTarget: false)
        
        let screenHeightInt = Int(screenHeight)
        
        //Output frequency text
        //frequency.text = String(format: "%0.1f", tracker.frequency)
//        print("frequency = \(tracker.frequency)")
//        print("amplitude = \(tracker.amplitude)")
        
        //Divide frequency until base value to identify
        var frequency = Float(tracker.frequency)
        while frequency > Float(targetArrayFrequencies[targetArrayFrequencies.count - 1]) {
            frequency /= 2.0
        }
        while frequency < Float(targetArrayFrequencies[0]) {
            frequency *= 2.0
        }
        let roundedFrequency = Float(String(format: "%.3f", frequency))

        //distance = measured-target
        var distance: Float = 10_000.0
        var centValue: Float = 0.0
        var centAmount: Float = 0.0
        var centAmountInt: Int = 0
        var y = Int(1.8*(screenHeight/2)-40)
        
        //minDistance identifies measured frequency
        var minDistance: Float = 10_000.0
        var index = 0
        
        //Identify the measured frequency note
        for i in 0..<targetArrayFrequencies.count {
            let distanceTest = fabsf(Float(targetArrayFrequencies[i]) - roundedFrequency!)
            if distanceTest < minDistance {
                index = i
                minDistance = distanceTest
            }
        }
        
        
        distance = fabsf(Float(targetArrayFrequencies[5]) - roundedFrequency!)
        
        //if measured frequency is greater than target note, move UI up
        if roundedFrequency! > Float(targetArrayFrequencies[5]) {
            
            centValue = Float((targetArrayFrequencies[6]-targetArrayFrequencies[5])/100)
            
            centAmount = distance / Float(centValue)
            centAmountInt = Int(centAmount)
            
            if centAmountInt < 100 {
                y = y - centAmountInt
            } else {
                y = y - 100
            }
            
        }
        //if measured frequency is less than target note, move UI down
        else if roundedFrequency! < Float(targetArrayFrequencies[5]) {
            centValue = Float((targetArrayFrequencies[5]-targetArrayFrequencies[4])/100)
            
            centAmount = distance / Float(centValue)
            centAmountInt = Int(centAmount)
            
            if centAmountInt < 100 {
                y = y + centAmountInt
            } else {
                y = y + 100
            }
        }
        //If measured frequency is equal to target, then UI goes in the middle of the screen
        else {
            centAmountInt = 0
            y = screenHeightInt/2

        }
        //print("cents = \(centAmountInt)")
        //Start or Stop timer based off of cent value
        if centAmountInt <= 50 && baseTimerDidStart == false {
            startBaseTimer()
            baseTimerDidStart = true
            
            UIView.animate(withDuration: 2.0, delay: 0.0, options: [],
            animations: {
                self.blueCircle.alpha = 0
            },
            completion: nil)
            
        } else if centAmountInt > 50 {
            timerBase.invalidate()
            baseSeconds = 1
            timerLabel.text = "\(baseSeconds)"
            baseTimerDidStart = false
            
            UIView.animate(withDuration: 0.01, delay: 0.0, options: [],
            animations: {
                self.blueCircle.alpha = 1
            },
            completion: nil)
        }
        //Put UI in center on the x axis and offset y by measured value
        Note.center = CGPoint(x: screenWidth/2, y: CGFloat(y))
        blueCircle.center = CGPoint(x: screenWidth/2, y: CGFloat(y))
        greenCircle.center = CGPoint(x: screenWidth/2, y: CGFloat(y))
        
        //Find octave of measured note
        let octave = Int(log2f(Float(tracker.frequency) / frequency))
        Note.text = "\(targetArrayNoteNames[index])\(octave)"
    }
    // MARK: - Interval Frequency Test
    func intervalFrequencyTest() {
        print("in intervalfrequencyTest...")
        //Shift base array to get interval array
        let baseArrayFrequencies = createNewDoubleArray(array: noteFrequencies, isTarget: false)
        let baseArrayNoteNames = createNewStringArray(array: noteNamesWithSharps, isTarget: false)
        let targetArrayFrequencies = createNewDoubleArray(array: baseArrayFrequencies, isTarget: true)
        let targetArrayNoteNames = createNewStringArray(array: baseArrayNoteNames, isTarget: true)
        
        let screenHeightInt = Int(screenHeight)
        
        //Output frequency text
        //frequency.text = String(format: "%0.1f", tracker.frequency)
        print("frequency = \(tracker.frequency)")
        print("amplitude = \(tracker.amplitude)")
        
        //Divide frequency until base value to identify
        var frequency = Float(tracker.frequency)
        while frequency > Float(targetArrayFrequencies[targetArrayFrequencies.count - 1]) {
            frequency /= 2.0
        }
        while frequency < Float(targetArrayFrequencies[0]) {
            frequency *= 2.0
        }
        let roundedFrequency = Float(String(format: "%.3f", frequency))

        //distance = measured-target
        var distance: Float = 10_000.0
        var centValue: Float = 0.0
        var centAmount: Float = 0.0
        var centAmountInt: Int = 0
        var y = screenHeightInt/2
        
        //minDistance identifies measured frequency
        var minDistance: Float = 10_000.0
        var index = 0
        
        //Identify the measured frequency note
        for i in 0..<targetArrayFrequencies.count {
            let distanceTest = fabsf(Float(targetArrayFrequencies[i]) - roundedFrequency!)
            if distanceTest < minDistance {
                index = i
                minDistance = distanceTest
            }
        }
        
        
        distance = fabsf(Float(targetArrayFrequencies[5]) - roundedFrequency!)
        
        //if measured frequency is greater than target note, move UI up
        if roundedFrequency! > Float(targetArrayFrequencies[5]) {
            
            centValue = Float((targetArrayFrequencies[6]-targetArrayFrequencies[5])/100)
            
            centAmount = distance / Float(centValue)
            centAmountInt = Int(centAmount)
            
            if centAmountInt < 200 {
                y = y - centAmountInt
            } else {
                y = y - 200
            }
            
        }
        //if measured frequency is less than target note, move UI down
        else if roundedFrequency! < Float(targetArrayFrequencies[5]) {
            centValue = Float((targetArrayFrequencies[5]-targetArrayFrequencies[4])/100)
            
            centAmount = distance / Float(centValue)
            centAmountInt = Int(centAmount)
            
            if centAmountInt < 200 {
                y = y + centAmountInt
            } else {
                y = y + 200
            }
        }
        //If measured frequency is equal to target, then UI goes in the middle of the screen
        else {
            centAmountInt = 0
            y = screenHeightInt/2

        }
        print("cents = \(centAmountInt)")
        //Start or Stop timer based off of cent value
        if centAmountInt <= 50 && intervalTimerDidStart == false {
            startIntervalTimer()
            intervalTimerDidStart = true
            
            UIView.animate(withDuration: 5.0, delay: 0.0, options: [],
            animations: {
                self.blueCircle.alpha = 0
            },
            completion: nil)
            
        } else if centAmountInt > 50 {
            timerInterval.invalidate()
            intervalSeconds = 3
            intervalTimerDidStart = false
            
            UIView.animate(withDuration: 0.01, delay: 0.0, options: [],
            animations: {
                self.blueCircle.alpha = 1
            },
            completion: nil)
        }
        
        //Put UI in center on the x axis and offset y by measured value
        Note.center = CGPoint(x: screenWidth/2, y: CGFloat(y))
        blueCircle.center = CGPoint(x: screenWidth/2, y: CGFloat(y))
        greenCircle.center = CGPoint(x: screenWidth/2, y: CGFloat(y))
        
        //Find octave of measured note
        let octave = Int(log2f(Float(tracker.frequency) / frequency))
        Note.text = "\(targetArrayNoteNames[index])\(octave)"
    }
    
    //Shift noteNames array left or right
    func createNewStringArray(array: Array<String>, isTarget: Bool) -> Array<String>{
        
        var indexDiff : Int
        
        if isTarget == false {
            indexDiff = randomNote - 5
        } else {
            indexDiff = randomInterval
        }
        var arr = array
                
        //if positive, shift left
        while (indexDiff > 0) {
            let first = arr[0]
            for i in 0..<arr.count - 1 {
                arr[i] = arr[i + 1]
            }
            arr[arr.count - 1] = first
            
            indexDiff -= 1
        }
            
        //if negative, shift right
        while (indexDiff < 0) {
            let last = arr[arr.count - 1]
            for i in (1..<arr.count).reversed() {
                arr[i] = arr[i - 1]
            }
            arr[0] = last
            indexDiff += 1
        }
        
        return arr
        
    }
    //Shift noteFrequencies array left or right and have frequencies from greatest to smallest
    func createNewDoubleArray(array: Array<Double>, isTarget: Bool) -> Array<Double>{
        
        var indexDiff : Int
        
        if isTarget == false {
            indexDiff = randomNote - 5
        } else {
            indexDiff = randomInterval
        }
        var arr = array
                
        //if positive, shift left
        while (indexDiff > 0) {
            let first = arr[0]
            for i in 0..<arr.count - 1 {
                arr[i] = arr[i + 1]
            }
            arr[arr.count - 1] = first
            arr[arr.count - 1] *= 2
            
            indexDiff -= 1
        }
            
        //if negative, shift right
        while (indexDiff < 0) {
            let last = arr[arr.count - 1]
            for i in (1..<arr.count).reversed() {
                arr[i] = arr[i - 1]
            }
            arr[0] = last
            arr[0] /= 2
            indexDiff += 1
        }
        
        return arr
        
    }
    
    //Code to play sound
    //Referenced code from https://gist.github.com/cliff538/91b8f8bf818d836e1d9537081d02c580
    func playSound(fileName : String) {

        let sound = Bundle.main.url(forResource: fileName, withExtension: "wav")
        do {
            audioPlayer = try AVAudioPlayer(contentsOf: sound!)
        }
        catch {
            print(error)
        }
        audioPlayer.play()
    }
    //https://medium.com/ios-os-x-development/build-an-stopwatch-with-swift-3-0-c7040818a10f
    
    @objc func updateTimer() {
        //if user gets base frequency correct, start interval timer
        if baseSeconds == 0 && intervalTimerDidStart == false{
            timerBase.invalidate()
            baseSeconds = 10
            timerLabel.text = "\(baseSeconds)"
            Note.center = CGPoint(x: screenWidth/2, y: (screenHeight/2))
            blueCircle.center = CGPoint(x: screenWidth/2, y: (screenHeight/2))
            greenCircle.center = CGPoint(x: screenWidth/2, y: (screenHeight/2))
            intervalFrequencyTest()
            passedBaseFrequencyTest = true
            startBaseTimer()
        }
        //if user runs out of time on interval timer, start over to base frequency
        else if baseSeconds == 0 && intervalTimerDidStart == true {
            timerBase.invalidate()
            timerInterval.invalidate()
            baseSeconds = 1
            intervalSeconds = 3
            timerLabel.text = "\(baseSeconds)"
            intervalTimerDidStart = false
            passedBaseFrequencyTest = false
            Note.center = CGPoint(x: screenWidth/2, y: (1.8*screenHeight/2-40))
            blueCircle.center = CGPoint(x: screenWidth/2, y: (1.8*screenHeight/2-40))
            greenCircle.center = CGPoint(x: screenWidth/2, y: (1.8*screenHeight/2-40))
            
        }
        //if user gets interval correct, score a point, stop timers, and reset RandomNote
        else if intervalSeconds == 0 {
            timerBase.invalidate()
            timerInterval.invalidate()
            randomNote = Int.random(in: 0..<12)
            targetNote.text = noteNamesWithSharps[randomNote]
            scoreCtr += 1
            scoreLabel.text = "\(scoreCtr) pts"
            passedBaseFrequencyTest = false
        }
        
        if intervalTimerDidStart == false {
            baseSeconds -= 1     //This will decrement(count down)the seconds.
            timerLabel.text = "\(baseSeconds)" //This will update the label.
        }
        else if intervalTimerDidStart == true {
            baseSeconds -= 1     //This will decrement(count down)the seconds.
            timerLabel.text = "\(baseSeconds)" //This will update the label.
            intervalSeconds -= 1
        }
        
    }
    func startBaseTimer() {
        timerBase = Timer.scheduledTimer(timeInterval: 1, target: self,   selector: (#selector(IntervalViewController.updateTimer)), userInfo: nil, repeats: true)
    }
    func startIntervalTimer() {
        timerInterval = Timer.scheduledTimer(timeInterval: 1, target: self,   selector: (#selector(IntervalViewController.updateTimer)), userInfo: nil, repeats: true)
    }
}

