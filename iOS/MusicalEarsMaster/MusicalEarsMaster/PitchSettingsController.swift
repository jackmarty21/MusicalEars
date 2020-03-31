//
//  PitchSettingsController.swift
//  MusicalEarsMaster
//
//  Created by Jack Marty on 3/31/20.
//  Copyright © 2020 Jack Marty. All rights reserved.
//

import UIKit

class PitchSettingsController: UIViewController {
    
    
    var noteCount = 10
    var difficulty = 1
    var showTarget = true
    var showInterval = true
    
    @IBOutlet weak var shortNoteButton: UIButton!
    @IBOutlet weak var mediumNoteButton: UIButton!
    @IBOutlet weak var longNoteButton: UIButton!
    @IBOutlet weak var easyDiffButton: UIButton!
    @IBOutlet weak var mediumDiffButton: UIButton!
    @IBOutlet weak var hardDiffButton: UIButton!
    @IBOutlet weak var diffText: UILabel!
    @IBOutlet weak var noteCountText: UILabel!
    @IBOutlet weak var showTargetSwitch: UISwitch!
    @IBOutlet weak var showIntervalTargetSwitch: UISwitch!
    
    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
    }
    
    @IBAction func shortNoteButtonTouched(_ sender: Any) {
        shortNoteButton.setImage(UIImage(named: "shortOn.png"), for: .normal)
        mediumNoteButton.setImage(UIImage(named: "mediumOff.png"), for: .normal)
        longNoteButton.setImage(UIImage(named: "longOff.png"), for: .normal)
        noteCountText.text = "10 notes will be matched"
        noteCount = 10
    }
    
    @IBAction func mediumNoteButtonTouched(_ sender: Any) {
        shortNoteButton.setImage(UIImage(named: "shortOff.png"), for: .normal)
        mediumNoteButton.setImage(UIImage(named: "mediumOn.png"), for: .normal)
        longNoteButton.setImage(UIImage(named: "longOff.png"), for: .normal)
        noteCountText.text = "20 notes will be matched"
        noteCount = 20
    }
    
    @IBAction func hardNoteButtonTouched(_ sender: Any) {
        shortNoteButton.setImage(UIImage(named: "shortOff.png"), for: .normal)
        mediumNoteButton.setImage(UIImage(named: "mediumOff.png"), for: .normal)
        longNoteButton.setImage(UIImage(named: "longOn.png"), for: .normal)
        noteCountText.text = "50 notes will be matched"
        noteCount = 50
    }
    
    @IBAction func easyDiffButtonTouched(_ sender: Any) {
        easyDiffButton.setImage(UIImage(named: "easyOn.png"), for: .normal)
        mediumDiffButton.setImage(UIImage(named: "mediumOff.png"), for: .normal)
        hardDiffButton.setImage(UIImage(named: "hardOff.png"), for: .normal)
        diffText.text = "Notes will be held for 1 second"
        difficulty = 1
    }
    @IBAction func mediumDiffButtonTouched(_ sender: Any) {
        easyDiffButton.setImage(UIImage(named: "easyOff.png"), for: .normal)
        mediumDiffButton.setImage(UIImage(named: "mediumOn.png"), for: .normal)
        hardDiffButton.setImage(UIImage(named: "hardOff.png"), for: .normal)
        diffText.text = "Notes will be held for 3 second"
        difficulty = 3
    }
    
    @IBAction func hardDiffButtonTouched(_ sender: Any) {
        easyDiffButton.setImage(UIImage(named: "easyOff.png"), for: .normal)
        mediumDiffButton.setImage(UIImage(named: "mediumOff.png"), for: .normal)
        hardDiffButton.setImage(UIImage(named: "hardOn.png"), for: .normal)
        diffText.text = "Notes will be held for 5 second"
        difficulty = 5
    }
    @IBAction func showTarget(_ sender: Any) {
        if showTargetSwitch.isOn {
            showTarget = true
        }
        else {
            showTarget = false
        }
    }
    @IBAction func showIntervalTarget(_ sender: Any) {
        if showIntervalTargetSwitch.isOn {
            showInterval = true
        }
        else {
            showInterval = false
        }
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "ShowPitch" {
            let pitchVC = segue.destination as! PitchViewController
            
            //set detail vc properties
            pitchVC.difficulty = difficulty
            pitchVC.seconds = difficulty
            pitchVC.noteCount = noteCount
            pitchVC.showTarget = showTarget
        }
        if segue.identifier == "ShowInterval" {
            let intervalVC = segue.destination as! IntervalViewController
            
            //set detail vc properties
            intervalVC.difficulty = difficulty
            intervalVC.noteCount = noteCount
            intervalVC.showTarget = showTarget
            intervalVC.showInterval = showInterval
        }
    }
    
}
