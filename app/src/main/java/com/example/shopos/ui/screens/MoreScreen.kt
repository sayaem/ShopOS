package com.example.shopos.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.shopos.ui.components.ShopOSCard
import com.example.shopos.ui.components.ShopOSTopBar
import com.example.shopos.ui.theme.*

@Composable
fun MoreScreen(
    onNavigateToRoute: (String) -> Unit
) {
    val context = LocalContext.current

    val moreItems = listOf(
        MoreCardItem("Business Profile", Icons.Outlined.PersonOutline, "settings"),
        MoreCardItem("Backup & Restore", Icons.Outlined.CloudUpload, "backup_restore"),
        MoreCardItem("Help & Support", Icons.Outlined.HelpOutline, "help"),
        MoreCardItem("Rate Us", Icons.Outlined.StarOutline, "rate"),
        MoreCardItem("Privacy Policy", Icons.Outlined.Shield, "privacy"),
        MoreCardItem("Terms & Conditions", Icons.Outlined.Description, "terms")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ShopPearlBackground)
            .padding(bottom = 80.dp)
    ) {
        ShopOSTopBar(
            title = "More"
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(moreItems) { item ->
                ShopOSCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(115.dp),
                    onClick = {
                        if (item.route == "rate") {
                            Toast.makeText(context, "Thank you for rating ShopOS 5 stars!", Toast.LENGTH_SHORT).show()
                        } else {
                            onNavigateToRoute(item.route)
                        }
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF3F3FA)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = ShopTextDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = ShopTextDark,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }
        }

        // Bottom Branding
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(ShopPrimaryLavender),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "ShopOS",
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "ShopOS",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = ShopTextDark
                )
            )
            Text(
                text = "Your Shop. Smarter.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = ShopTextMuted,
                    fontSize = 12.sp
                )
            )
        }
    }
}

data class MoreCardItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)
