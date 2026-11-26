package com.example.sakuraevents

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.google.apps.card.v1.Columns


@Composable
fun PolicyScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(COLOURS.LIGHTYELLOW.colour)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start

    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF006400))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(40.dp)
        ) {
            Text(
                text = "App Policy and Guidelines",
                fontSize = 24.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            HorizontalDivider(
                color = Color.White,
                thickness = 2.dp
            )
            Text(
                text = "Welcome to Sakura Events",
                color = Color.White,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Campsite Visitor Guidelines",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            // List of guidelines.
            val guidelines = listOf(
                "Make a reservation before visiting.",
                "Arrive on your scheduled date.",
                "Respect other campers and keep noise low.",
                " Keep the campsite clean.",
                " Follow campsite safety rules.",
                " Cancel your reservation if you can not attend.",
                " Leave an honest rating after your stay."
            )
            // Display each guideline
            guidelines.forEachIndexed { index, guideline ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ){
                    Box(
                        modifier = Modifier.size(32.dp)
                            .background(Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ){
                        Text(
                            text = "${index + 1}",
                            color = Color(0xFF006400),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    // GuideLine text
                    Text(
                        text = guideline,
                        color= Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                }

            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun PolicyScreenPreview() {
    PolicyScreen()
}
