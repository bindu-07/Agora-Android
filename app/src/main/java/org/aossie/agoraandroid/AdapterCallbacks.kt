package org.aossie.agoraandroid

interface ElectionAdapterCallback{
  fun onItemClicked(electionName: String,
    electionDesc: String,
    startDate: String,
    endDate:String,
    status: String,
    candidate :String,
    id: String)
}